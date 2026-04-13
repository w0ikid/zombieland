<script lang="ts">
  import { createDistrict, getAllDistricts, getDistrictById, createSortie } from '$lib/api';
  import { getResources, addResource } from '$lib/api';
  import type { DistrictDTO, ResourceDTO, SortieOutcome } from '$lib/api';
  import { userManager, hasRole } from '$lib/auth';
  import { onMount } from 'svelte';
  import type { Map, Marker } from 'leaflet';
  import { tick } from 'svelte';
  
  let name = $state('');
  let lat = $state(0);
  let lng = $state(0);
  let error = $state('');
  let loading = $state(false);
  let districts = $state<DistrictDTO[]>([]);

  // Модалка
  let selectedDistrict = $state<DistrictDTO | null>(null);
  let resources = $state<ResourceDTO[]>([]);
  let loadingResources = $state(false);
  let isSupport = $state(false);

  // Форма добавления ресурса
  let resType = $state<'FOOD' | 'AMMO' | 'MATERIALS'>('FOOD');
  let resAmount = $state(0);
  let addingResource = $state(false);
  let userId = $state<string | null>(null);

  // Вылазка (Sortie)
  let sortieAction = $state('');
  let sortieOutcome = $state<SortieOutcome | null>(null);
  let performingSortie = $state(false);

  let mapEl: HTMLDivElement;
  let map: Map;
  let tempMarker: Marker | null = null;

  function survivalColor(index: number, isActive: boolean = true): string {
    if (!isActive) return '#6b7280'; // Gray
    if (index >= 70) return '#22c55e';
    if (index >= 40) return '#f59e0b';
    return '#ef4444';
  }

  function survivalLabel(index: number): string {
    if (index >= 70) return 'СТАБИЛЬНО';
    if (index >= 40) return 'ОПАСНО';
    return 'КРИТИЧНО';
  }

  async function openDistrict(d: DistrictDTO) {
    console.log('openDistrict called', d);
    selectedDistrict = d;
    sortieAction = '';
    sortieOutcome = null;
    console.log('selectedDistrict set', selectedDistrict);
    await tick();
    console.log('after tick, selectedDistrict', selectedDistrict);
    loadingResources = true;
    resources = await getResources(d.id);
    loadingResources = false;
}

  async function refreshCurrentDistrict() {
    if (!selectedDistrict) return;
    const updated = await getDistrictById(selectedDistrict.id);
    selectedDistrict = updated;
    const index = districts.findIndex(d => d.id === updated.id);
    if (index !== -1) {
      districts[index] = updated;
    }
  }

  function closeModal() {
    selectedDistrict = null;
    resources = [];
    resAmount = 0;
    sortieAction = '';
    sortieOutcome = null;
  }

  async function handleSortie() {
    if (!selectedDistrict || !sortieAction) return;
    performingSortie = true;
    try {
      const result = await createSortie(selectedDistrict.id, sortieAction);
      sortieOutcome = result;
      // Обновляем данные района и ресурсы
      await refreshCurrentDistrict();
      resources = await getResources(selectedDistrict.id);
    } catch (e) {
      console.error(e);
    } finally {
      performingSortie = false;
    }
  }

  async function handleAddResource() {
    if (!selectedDistrict) return;
    addingResource = true;
    try {
      const created = await addResource(selectedDistrict.id, { type: resType as any, amount: resAmount });
      resources = [...resources, created];
      resAmount = 0;
    } finally {
      addingResource = false;
    }
  }

    async function addDistrictToMap(L: typeof import('leaflet'), d: DistrictDTO) {
        const color = survivalColor(d.survivalIndex, d.isActive);
        const size = 14;
        const pulse = d.isActive && d.survivalIndex < 70;
        const fast = d.isActive && d.survivalIndex < 40;

        const icon = L.divIcon({
            className: '',
            html: `
            <style>
                @keyframes ping-${d.id} {
                0% { transform: scale(1); opacity: 0.4; }
                100% { transform: scale(3); opacity: 0; }
                }
            </style>
            <div style="position:relative;width:${size}px;height:${size}px">
                ${pulse ? `
                <div style="
                    position:absolute;inset:0;border-radius:50%;
                    background:${color};opacity:0.4;
                    animation:ping-${d.id} ${fast ? '0.8s' : '1.6s'} ease-out infinite;
                "></div>
                ` : ''}
                <div style="
                position:absolute;inset:0;border-radius:50%;
                background:${color};
                border:2px solid var(--bg-color);
                box-shadow:0 0 ${fast ? '12px' : '6px'} ${color};
                "></div>
            </div>
            `,
            iconSize: [size, size],
            iconAnchor: [size / 2, size / 2],
        });

        const marker = L.marker([d.lat, d.lng], { icon }).addTo(map);

        const popupEl = document.createElement('div');
        popupEl.style.cssText = `font-family:'Share Tech Mono',monospace;font-size:11px;color:var(--text-color);background:var(--card-bg);padding:8px;border:1px solid ${color};min-width:160px`;
        popupEl.innerHTML = `
            <div style="color:var(--accent-color);margin-bottom:4px">${d.name}</div>
            <div>SURVIVAL: <span style="color:${color}">${d.survivalIndex}</span></div>
            <div>STATUS: <span style="color:${color}">${survivalLabel(d.survivalIndex)}</span></div>
            <div style="margin-top:8px">
            <button data-id="${d.id}" style="
                font-family:'Share Tech Mono',monospace;font-size:10px;
                border:1px solid var(--accent-color);background:transparent;color:var(--text-color);
                padding:3px 8px;cursor:pointer;letter-spacing:0.1em
            ">[ ДЕТАЛИ ]</button>
            </div>
        `;

        // Вешаем обработчик на кнопку напрямую
        popupEl.querySelector('button')?.addEventListener('click', () => {
            console.log('button clicked', d);
            marker.closePopup();
            openDistrict(d);
        });

        marker.bindPopup(popupEl);
    }

  onMount(async () => {
    // 1. Инициализация карты сразу, чтобы она не зависела от других запросов
    const L = await import('leaflet');
    await import('leaflet/dist/leaflet.css');

    map = L.map(mapEl, { zoomControl: false }).setView([43.238, 76.889], 12);

    let activeLayer: any;
    const updateTiles = () => {
      const isDark = document.documentElement.classList.contains('dark');
      const url = isDark 
        ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
        : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
      
      if (activeLayer) map.removeLayer(activeLayer);
      activeLayer = L.tileLayer(url, {
        attribution: '© OpenStreetMap © CARTO'
      }).addTo(map);
    };

    updateTiles();

    // Наблюдатель за сменой темы
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.attributeName === 'class') {
          updateTiles();
        }
      });
    });
    observer.observe(document.documentElement, { attributes: true });

    L.control.zoom({ position: 'bottomright' }).addTo(map);

    map.on('click', (e) => {
      lat = parseFloat(e.latlng.lat.toFixed(6));
      lng = parseFloat(e.latlng.lng.toFixed(6));
      if (tempMarker) tempMarker.remove();

      const icon = L.divIcon({
        className: '',
        html: `<div style="
          width:16px;height:16px;border-radius:50%;
          border:2px dashed var(--accent-color);
          box-shadow:0 0 10px var(--accent-color);
        "></div>`,
        iconSize: [16, 16],
        iconAnchor: [8, 8],
      });

      tempMarker = L.marker([lat, lng], { icon })
        .addTo(map)
        .bindPopup(`
          <div style="font-family:'Share Tech Mono',monospace;font-size:11px;color:var(--text-color);background:var(--card-bg);padding:6px;border:1px solid var(--accent-color)">
            📍 ${lat}, ${lng}
          </div>
        `)
        .openPopup();
    });

    // 2. Параллельная загрузка данных пользователя и районов
    const [user, loadedDistricts] = await Promise.all([
      userManager.getUser(),
      getAllDistricts()
    ]);

    isSupport = hasRole(user, 'support') || hasRole(user, 'admin');
    userId = user?.profile.sub ?? null;
    districts = loadedDistricts;

    // Глобальный хук для кнопки в popup
    (window as any).openDistrictModal = async (id: number) => {
      const d = districts.find(x => x.id === id);
      if (d) await openDistrict(d);
    };

    for (const d of districts) {
      await addDistrictToMap(L, d);
    }
  });

  async function handleSubmit() {
    loading = true;
    error = '';
    try {
      const created = await createDistrict({ name, lat, lng });
      districts = [...districts, created];
      const L = await import('leaflet');
      if (tempMarker) { tempMarker.remove(); tempMarker = null; }
      await addDistrictToMap(L, created);
      name = '';
      lat = 0;
      lng = 0;
    } catch (e) {
      error = 'ОШИБКА СОЕДИНЕНИЯ';
    } finally {
      loading = false;
    }
  }

  const resourceIcon: Record<string, string> = {
    FOOD: '🍖',
    AMMO: '🔴',
    MATERIALS: '🔩',
  };
</script>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Share+Tech+Mono&display=swap');

  .font-bebas { font-family: 'Bebas Neue', sans-serif; }
  .font-mono-tech { font-family: 'Share Tech Mono', monospace; }

  .grid-bg {
    background-image:
      linear-gradient(var(--grid-color) 1px, transparent 1px),
      linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
    background-size: 40px 40px;
  }

  .blink { animation: blink 1.2s step-end infinite; }
  @keyframes blink {
    0%, 100% { opacity: 1; } 50% { opacity: 0; }
  }

  .btn-fill {
    position: relative;
    overflow: hidden;
    transition: color 0.3s;
  }
  .btn-fill::before {
    content: '';
    position: absolute;
    inset: 0;
    background: var(--accent-color);
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    z-index: -1;
  }
  .btn-fill:hover::before { transform: translateX(0); }
  .btn-fill:hover { color: var(--bg-color); }

  .scanlines {
    background: repeating-linear-gradient(
      0deg, transparent, transparent 2px,
      var(--scanline-color) 2px, var(--scanline-color) 4px
    );
  }

  :global(.leaflet-popup-content-wrapper),
  :global(.leaflet-popup-tip) {
    background: transparent !important;
    box-shadow: none !important;
    padding: 0 !important;
  }
  :global(.leaflet-popup-content) { margin: 0 !important; }

  .scroll-list::-webkit-scrollbar { width: 3px; }
  .scroll-list::-webkit-scrollbar-track { background: var(--bg-color); }
  .scroll-list::-webkit-scrollbar-thumb { background: var(--accent-color); }
</style>

<div class="scanlines fixed inset-0 pointer-events-none z-50"></div>

<div class="font-mono-tech flex h-screen bg-[var(--bg-color)] text-[var(--text-color)] overflow-hidden">

  <!-- Сайдбар -->
  <div class="grid-bg relative w-80 flex flex-col gap-4 p-5 border-r border-[var(--accent-color)]/30 z-10 bg-[var(--sidebar-bg)]">
    <div class="absolute top-3 left-3 w-4 h-4 border-t border-l border-[var(--accent-color)]/50"></div>
    <div class="absolute top-3 right-3 w-4 h-4 border-t border-r border-[var(--accent-color)]/50"></div>

    <div class="pt-2">
      <div class="text-[0.6rem] tracking-[0.3em] text-[var(--accent-color)] mb-1">ZONE CONTROL // DISTRICTS</div>
      <h1 class="font-bebas text-3xl tracking-widest">УПРАВЛЕНИЕ ЗОНАМИ</h1>
    </div>

    <div class="border-t border-[var(--accent-color)]/20"></div>

    <div class="flex flex-col gap-3">
      <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)]">// РЕГИСТРАЦИЯ РАЙОНА</div>

      {#if error}
        <div class="text-[0.7rem] text-red-500 border border-red-500/40 px-3 py-2 tracking-wider">
          ⚠ {error}
        </div>
      {/if}

      <input
        bind:value={name}
        class="bg-transparent border border-[var(--accent-color)]/40 text-[var(--text-color)] text-[0.75rem] tracking-wider px-3 py-2 outline-none focus:border-[var(--accent-color)] placeholder-[var(--muted-color)]"
        placeholder="НАЗВАНИЕ РАЙОНА"
      />

      <div class="text-[0.65rem] tracking-wider text-[var(--muted-color)] border border-dashed border-[var(--border-color)] px-3 py-2">
        {#if lat || lng}
          <span class="text-[var(--accent-color)]">▸</span> LAT: <span class="text-[var(--text-color)]">{lat}</span><br/>
          <span class="text-[var(--accent-color)]">▸</span> LNG: <span class="text-[var(--text-color)]">{lng}</span>
        {:else}
          <span class="blink text-[var(--accent-color)]">_</span> КЛИКНИ НА КАРТУ
        {/if}
      </div>

      <button
        onclick={handleSubmit}
        disabled={loading || !name || (!lat && !lng)}
        class="btn-fill text-[0.75rem] tracking-[0.2em] px-4 py-3 border border-[var(--accent-color)] text-[var(--text-color)] cursor-pointer bg-transparent disabled:opacity-30 disabled:cursor-not-allowed"
      >
        {loading ? '[ СОЗДАНИЕ... ]' : '[ ЗАРЕГИСТРИРОВАТЬ ]'}
      </button>
    </div>

    <div class="border-t border-[var(--accent-color)]/20"></div>

    <div class="flex flex-col gap-1.5">
      <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)]">// SURVIVAL INDEX</div>
      <div class="flex items-center gap-2 text-[0.65rem] tracking-wider">
        <span class="w-2.5 h-2.5 rounded-full bg-green-500 shadow-[0_0_6px_#22c55e] shrink-0"></span>
        <span class="text-[var(--muted-color)]">70–100</span> СТАБИЛЬНО
      </div>
      <div class="flex items-center gap-2 text-[0.65rem] tracking-wider">
        <span class="w-2.5 h-2.5 rounded-full bg-yellow-400 shadow-[0_0_6px_#f59e0b] shrink-0"></span>
        <span class="text-[var(--muted-color)]">40–69</span> ОПАСНО
      </div>
      <div class="flex items-center gap-2 text-[0.65rem] tracking-wider">
        <span class="w-2.5 h-2.5 rounded-full bg-red-500 shadow-[0_0_6px_#ef4444] shrink-0"></span>
        <span class="text-[var(--muted-color)]">0–39</span> КРИТИЧНО
      </div>
    </div>

    <div class="border-t border-[var(--accent-color)]/20"></div>

    <div class="scroll-list flex flex-col gap-2 overflow-y-auto flex-1">
      <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)]">// АКТИВНЫЕ ЗОНЫ [{districts.length}]</div>
      {#each districts as d}
        {@const color = survivalColor(d.survivalIndex, d.isActive)}
        <button
          onclick={() => openDistrict(d)}
          class="border border-[var(--border-color)] px-3 py-2 text-[0.7rem] tracking-wider text-left hover:border-[var(--accent-color)]/40 transition-colors w-full"
          style="border-left: 2px solid {color}"
        >
          <div class="text-[var(--text-color)]">{d.name}</div>
          <div class="flex justify-between mt-0.5">
            <span class="text-[var(--muted-color)]">SURVIVAL</span>
            <span style="color:{color}">{d.survivalIndex} // {survivalLabel(d.survivalIndex)}</span>
          </div>
        </button>
      {/each}
    </div>

    <div class="absolute bottom-3 left-3 w-4 h-4 border-b border-l border-[var(--accent-color)]/50"></div>
    <div class="absolute bottom-3 right-3 w-4 h-4 border-b border-r border-[var(--accent-color)]/50"></div>
  </div>

  <!-- Карта -->
  <div bind:this={mapEl} class="flex-1"></div>
</div>

<!-- Модалка -->
{#if selectedDistrict}
  {@const d = selectedDistrict}
  {@const color = survivalColor(d.survivalIndex, d.isActive)}

  <!-- Оверлей -->
  <div
    class="fixed inset-0 bg-black/70 z-[9998]"
    onclick={closeModal}
></div>

  <!-- Панель -->
  <div class="font-mono-tech fixed top-0 right-0 h-full w-96 bg-[var(--card-bg)] border-l border-[var(--accent-color)]/40 z-[9999] flex flex-col overflow-hidden">

    <!-- Угловые декорации -->
    <div class="absolute top-3 left-3 w-4 h-4 border-t border-l border-[var(--accent-color)]/50"></div>
    <div class="absolute top-3 right-3 w-4 h-4 border-t border-r border-[var(--accent-color)]/50"></div>

    <!-- Заголовок -->
    <div class="p-5 border-b border-[var(--accent-color)]/20">
      <div class="text-[0.6rem] tracking-[0.3em] text-[var(--muted-color)] mb-1">ZONE INTEL // DISTRICT DATA</div>
      <div class="font-bebas text-2xl tracking-widest text-[var(--text-color)]">{d.name}</div>
      <button onclick={closeModal} class="absolute top-4 right-5 text-[var(--muted-color)] hover:text-[var(--text-color)] text-lg">✕</button>
    </div>

    <div class="flex-1 overflow-y-auto scroll-list p-5 flex flex-col gap-5">

      <!-- Основные данные -->
      <div class="flex flex-col gap-2">
        <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)]">// СТАТУС БАЗЫ</div>

        <div class="border border-[var(--border-color)] p-3 flex flex-col gap-1.5 text-[0.7rem] tracking-wider">
          <div class="flex justify-between">
            <span class="text-[var(--muted-color)]">SURVIVAL INDEX</span>
            <span style="color:{color}" class="font-bold">{d.survivalIndex}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-[var(--muted-color)]">СТАТУС</span>
            <span style="color:{color}">{survivalLabel(d.survivalIndex)}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-[var(--muted-color)]">АКТИВНА</span>
            <span class="text-[var(--text-color)]">{d.isActive ? 'ДА' : 'НЕТ'}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-[var(--muted-color)]">КООРДИНАТЫ</span>
            <span class="text-[var(--text-color)]">{d.lat}, {d.lng}</span>
          </div>
        </div>

        <!-- Бар выживаемости -->
        <div class="h-1.5 bg-[var(--border-color)] rounded-full overflow-hidden">
          <div
            class="h-full rounded-full transition-all"
            style="width:{d.survivalIndex}%;background:{color};box-shadow:0 0 8px {color}"
          ></div>
        </div>
      </div>

      <!-- Ресурсы -->
      <div class="flex flex-col gap-2">
        <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)]">// РЕСУРСЫ</div>

        {#if loadingResources}
          <div class="text-[0.65rem] text-[var(--muted-color)] tracking-wider">ЗАГРУЗКА...</div>
        {:else if resources.length === 0}
          <div class="text-[0.65rem] text-[var(--muted-color)] tracking-wider border border-dashed border-[var(--border-color)] p-3">
            РЕСУРСЫ НЕ НАЙДЕНЫ
          </div>
        {:else}
          {#each resources as r}
            <div class="border border-[var(--border-color)] px-3 py-2 flex justify-between text-[0.7rem] tracking-wider">
              <span class="text-[var(--muted-color)]">{resourceIcon[r.type] ?? '▸'} {r.type}</span>
              <span class="text-[var(--text-color)]">{r.amount}</span>
            </div>
          {/each}
        {/if}
      </div>

      <!-- Добавление ресурса (только support/admin) -->
      {#if isSupport}
        <div class="flex flex-col gap-2">
          <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)]">// ДОБАВИТЬ РЕСУРС</div>

          <select
            bind:value={resType}
            class="bg-transparent border border-[var(--accent-color)]/40 text-[var(--text-color)] text-[0.7rem] tracking-wider px-3 py-2 outline-none"
          >
            <option value="FOOD">🍖 FOOD</option>
            <option value="AMMO">🔴 AMMO</option>
            <option value="MATERIALS">🔩 MATERIALS</option>
          </select>

          <input
            type="number"
            bind:value={resAmount}
            min="0"
            class="bg-transparent border border-[var(--accent-color)]/40 text-[var(--text-color)] text-[0.7rem] tracking-wider px-3 py-2 outline-none focus:border-[var(--accent-color)]"
            placeholder="КОЛИЧЕСТВО"
          />

          <button
            onclick={handleAddResource}
            disabled={addingResource || resAmount <= 0 || !d.isActive}
            class="btn-fill text-[0.7rem] tracking-[0.2em] px-4 py-2.5 border border-[var(--accent-color)] text-[var(--text-color)] cursor-pointer bg-transparent disabled:opacity-30 disabled:cursor-not-allowed"
          >
            {!d.isActive ? '[ ЗОНА НЕДОСТУПНА ]' : addingResource ? '[ ДОБАВЛЕНИЕ... ]' : '[ ДОБАВИТЬ ]'}
          </button>
        </div>
      {/if}

      <!-- Вылазка (Sortie) -->
      {#if !d.owner || d.owner === userId}
      <div class="flex flex-col gap-2">
        <div class="text-[0.6rem] tracking-[0.25em] text-[var(--accent-color)] mb-1">// ВЫЛАЗКА В ГОРОД</div>

        {#if !sortieOutcome}
          <textarea
            bind:value={sortieAction}
            class="bg-transparent border border-[var(--accent-color)]/30 text-[var(--text-color)] text-[0.7rem] tracking-wider px-3 py-2 outline-none focus:border-[var(--accent-color)] min-h-[80px] resize-none"
            placeholder="ОПИШИТЕ ВАШИ ДЕЙСТВИЯ..."
          ></textarea>

          <button
            onclick={handleSortie}
            disabled={performingSortie || !sortieAction || !d.isActive}
            class="btn-fill text-[0.7rem] tracking-[0.2em] px-4 py-2.5 border border-[var(--accent-color)] text-[var(--text-color)] cursor-pointer bg-transparent disabled:opacity-30 disabled:cursor-not-allowed"
          >
            {!d.isActive ? '[ СВЯЗЬ ПОТЕРЯНА ]' : performingSortie ? '[ СВЯЗЬ С ИИ... ]' : '[ ОТПРАВИТЬ ]'}
          </button>
        {:else}
          <!-- Отчет о миссии -->
          <div class="border border-[var(--accent-color)]/50 bg-[var(--accent-color)]/5 p-4 flex flex-col gap-3 relative overflow-hidden">
            <div class="absolute top-0 right-0 px-2 py-0.5 bg-[var(--accent-color)] text-[var(--bg-color)] font-bold tracking-tighter">
              REPORT_{Math.floor(Math.random() * 9999)}
            </div>
            
            <div class="text-[0.65rem] text-[var(--accent-color)] font-bold tracking-[0.2em] border-b border-[var(--accent-color)]/20 pb-1">
              ОТЧЕТ О ВЫЛАЗКЕ
            </div>

            <p class="text-[0.7rem] leading-relaxed text-[var(--text-color)]/90 italic">
              « {sortieOutcome.description} »
            </p>

            <div class="flex flex-col gap-1 mt-1">
              <div class="flex justify-between text-[0.6rem] tracking-wider">
                <span class="text-[var(--muted-color)]">РЕЗУЛЬТАТ</span>
                <span class={sortieOutcome.outcome === 'success' ? 'text-green-500' : sortieOutcome.outcome === 'fail' ? 'text-red-500' : 'text-yellow-500'}>
                  {sortieOutcome.outcome.toUpperCase()}
                </span>
              </div>
              
              {#if Object.keys(sortieOutcome.resources).length > 0}
                <div class="text-[0.55rem] tracking-[0.2em] text-[var(--muted-color)] mt-2 border-t border-[var(--accent-color)]/10 pt-2">ИЗМЕНЕНИЯ РЕСУРСОВ:</div>
                {#each Object.entries(sortieOutcome.resources) as [type, amount]}
                    {#if amount !== 0}
                        <div class="flex justify-between text-[0.65rem] tracking-wider">
                            <span class="text-[#888]">{type}</span>
                            <span class={amount > 0 ? 'text-green-500' : 'text-red-500'}>
                                {amount > 0 ? '+' : ''}{amount}
                            </span>
                        </div>
                    {/if}
                {/each}
              {/if}
            </div>

            <button
              onclick={() => { sortieOutcome = null; sortieAction = ''; }}
              class="mt-2 text-[0.6rem] tracking-[0.2em] text-[var(--accent-color)] hover:text-[var(--text-color)] text-left underline"
            >
              [ НОВАЯ ОПЕРАЦИЯ ]
            </button>
          </div>
        {/if}
      </div>
      {:else}
      <div class="text-[0.6rem] tracking-[0.25em] text-[var(--muted-color)] mt-4 border border-[var(--border-color)] p-3 text-center">
        ЭТОТ РАЙОН ПРИНАДЛЕЖИТ ДРУГОЙ ГРУППЕ
      </div>
      {/if}
    </div>

    <div class="absolute bottom-3 left-3 w-4 h-4 border-b border-l border-[var(--accent-color)]/50"></div>
    <div class="absolute bottom-3 right-3 w-4 h-4 border-b border-r border-[var(--accent-color)]/50"></div>
  </div>
{/if}