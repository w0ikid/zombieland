<script lang="ts">
  import { onMount } from 'svelte';
  import { userManager } from '$lib/auth';
  import { getMyAccount } from '$lib/api/account';
  import { toTenge, toTiyin } from '$lib/money';
  import { apiFetch } from '$lib/api/client';

  let account = $state<{
    id: string;
    number: string;
    balance: number;
    currency: string;
  } | null>(null);

  let loading = $state(true);

  // Форма перевода
  let toNumber = $state('');
  let amountTenge = $state('');
  let transferring = $state(false);
  let transferError = $state('');
  let transferSuccess = $state('');

  const amountTiyin = $derived(
    amountTenge ? toTiyin(parseFloat(amountTenge)) : 0
  );

  const canTransfer = $derived(
    !transferring &&
    toNumber.trim().length > 0 &&
    amountTiyin > 0 &&
    account !== null &&
    amountTiyin <= account.balance
  );

  onMount(async () => {
    const user = await userManager.getUser();
    if (!user) return;
    account = await getMyAccount(user.profile.sub);
    loading = false;
  });

  async function handleTransfer() {
    if (!canTransfer) return;
    transferring = true;
    transferError = '';
    transferSuccess = '';

    try {
      const res = await apiFetch('/api/v1/transactions/transfer', {
        method: 'POST',
        body: JSON.stringify({
          to_account_number: toNumber.trim(),
          amount: amountTiyin,
          currency: 'KZT',
          idempotency_key: `transfer-${Date.now()}`,
        }),
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error ?? 'Ошибка');
      }

      if (account) account = { ...account, balance: account.balance - amountTiyin };
      transferSuccess = `ПЕРЕВЕДЕНО ${amountTenge} ₸ → ${toNumber.trim()}`;
      toNumber = '';
      amountTenge = '';
    } catch (e: any) {
      transferError = e.message ?? 'ОШИБКА ПЕРЕВОДА';
    } finally {
      transferring = false;
    }
  }
</script>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Share+Tech+Mono&display=swap');

  .font-bebas { font-family: 'Bebas Neue', sans-serif; }
  .font-mono  { font-family: 'Share Tech Mono', monospace; }

  .grid-bg {
    background-image:
      linear-gradient(var(--grid-color) 1px, transparent 1px),
      linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
    background-size: 40px 40px;
  }

  .scanlines {
    background: repeating-linear-gradient(
      0deg, transparent, transparent 2px,
      var(--scanline-color) 2px, var(--scanline-color) 4px
    );
  }

  .blink { animation: blink 1.2s step-end infinite; }
  @keyframes blink { 0%,100%{opacity:1} 50%{opacity:0} }

  .fade-in { animation: fadeIn 0.4s ease; }
  @keyframes fadeIn { from{opacity:0;transform:translateY(6px)} to{opacity:1;transform:translateY(0)} }

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
  .btn-fill:hover:not(:disabled)::before { transform: translateX(0); }
  .btn-fill:hover:not(:disabled) { color: var(--bg-color); }

  input {
    background: transparent;
    border: 1px solid var(--border-color);
    color: var(--text-color);
    font-family: 'Share Tech Mono', monospace;
    font-size: 0.75rem;
    letter-spacing: 0.1em;
    padding: 8px 12px;
    outline: none;
    width: 100%;
    box-sizing: border-box;
  }
  input:focus { border-color: var(--accent-color); }
  input::placeholder { color: var(--muted-color); }
  input[type=number]::-webkit-inner-spin-button,
  input[type=number]::-webkit-outer-spin-button { -webkit-appearance: none; }
</style>

<div class="scanlines fixed inset-0 pointer-events-none z-50"></div>

<div class="font-mono min-h-screen bg-[var(--bg-color)] text-[var(--text-color)] grid-bg">

  <header style="border-bottom:1px solid var(--border-color);padding:16px 32px;display:flex;align-items:center;justify-content:space-between">
    <div>
      <div style="font-size:0.55rem;letter-spacing:0.35em;color:var(--accent-color);margin-bottom:2px">ZONE CONTROL // FINANCE</div>
      <h1 class="font-bebas" style="font-size:2rem;letter-spacing:0.15em;line-height:1">КОШЕЛЁК ВЫЖИВШЕГО</h1>
    </div>
    <a
      href="/"
      style="font-size:0.65rem;letter-spacing:0.2em;color:var(--muted-color);border:1px solid var(--border-color);padding:6px 14px;text-decoration:none;transition:color 0.2s,border-color 0.2s"
      onmouseenter={e => { (e.target as HTMLElement).style.color='var(--text-color)'; (e.target as HTMLElement).style.borderColor='var(--accent-color)'; }}
      onmouseleave={e => { (e.target as HTMLElement).style.color='var(--muted-color)'; (e.target as HTMLElement).style.borderColor='var(--border-color)'; }}
    >[ ← КАРТА ]</a>
  </header>

  <div style="padding:32px;max-width:480px;display:flex;flex-direction:column;gap:24px">

    {#if loading}
      <div style="font-size:0.7rem;letter-spacing:0.2em;color:var(--muted-color)">
        <span class="blink">_</span> ЗАГРУЗКА ДАННЫХ...
      </div>
    {:else if account}

      <!-- Карточка счёта -->
      <div class="fade-in" style="border:1px solid var(--border-color);padding:24px;position:relative">
        <div style="position:absolute;top:8px;left:8px;width:12px;height:12px;border-top:1px solid var(--border-color);border-left:1px solid var(--border-color)"></div>
        <div style="position:absolute;top:8px;right:8px;width:12px;height:12px;border-top:1px solid var(--border-color);border-right:1px solid var(--border-color)"></div>
        <div style="position:absolute;bottom:8px;left:8px;width:12px;height:12px;border-bottom:1px solid var(--border-color);border-left:1px solid var(--border-color)"></div>
        <div style="position:absolute;bottom:8px;right:8px;width:12px;height:12px;border-bottom:1px solid var(--border-color);border-right:1px solid var(--border-color)"></div>

        <div style="font-size:0.55rem;letter-spacing:0.3em;color:var(--muted-color);margin-bottom:16px">// СЧЁТ</div>

        <div style="font-size:0.65rem;letter-spacing:0.15em;color:var(--muted-color);margin-bottom:4px">НОМЕР СЧЁТА</div>
        <div style="font-size:0.85rem;letter-spacing:0.2em;color:var(--text-color);margin-bottom:20px;border-bottom:1px solid var(--border-color);padding-bottom:16px">
          {account.number}
        </div>

        <div style="font-size:0.65rem;letter-spacing:0.15em;color:var(--muted-color);margin-bottom:6px">БАЛАНС</div>
        <div class="font-bebas" style="font-size:3.5rem;letter-spacing:0.05em;line-height:1;color:var(--text-color)">
          {toTenge(account.balance)}<span style="font-size:1.8rem;color:var(--accent-color);margin-left:6px">₸</span>
        </div>
        <div style="font-size:0.6rem;letter-spacing:0.2em;color:var(--muted-color);margin-top:4px">
          {account.balance.toLocaleString()} ТЫЙЫН
        </div>

        <div style="margin-top:20px;height:1px;background:var(--border-color);position:relative">
          <div style="position:absolute;top:-1px;left:0;height:3px;background:var(--accent-color);width:{Math.min(account.balance/1000000*100,100)}%;box-shadow:0 0 8px var(--accent-color);transition:width 0.6s ease"></div>
        </div>
        <div style="display:flex;justify-content:space-between;margin-top:4px;font-size:0.55rem;letter-spacing:0.1em;color:var(--muted-color)">
          <span>0 ₸</span><span>10 000 ₸</span>
        </div>
      </div>

      <!-- Форма перевода -->
      <div class="fade-in" style="border:1px solid var(--border-color);padding:20px;display:flex;flex-direction:column;gap:14px">
        <div style="font-size:0.55rem;letter-spacing:0.3em;color:var(--muted-color)">// ПЕРЕВОД</div>

        {#if transferSuccess}
          <div style="font-size:0.7rem;letter-spacing:0.12em;color:#22c55e;border:1px solid rgba(34,197,94,0.2);padding:10px">
            ✓ {transferSuccess}
          </div>
        {/if}

        {#if transferError}
          <div style="font-size:0.7rem;letter-spacing:0.12em;color:#ef4444;border:1px solid rgba(239,68,68,0.2);padding:10px">
            ⚠ {transferError}
          </div>
        {/if}

        <div style="display:flex;flex-direction:column;gap:6px">
          <div style="font-size:0.6rem;letter-spacing:0.2em;color:var(--muted-color)">НОМЕР СЧЁТА ПОЛУЧАТЕЛЯ</div>
          <input bind:value={toNumber} placeholder="KZ00000000000000000" />
        </div>

        <div style="display:flex;flex-direction:column;gap:6px">
          <div style="font-size:0.6rem;letter-spacing:0.2em;color:var(--muted-color)">СУММА (ТЕНГЕ)</div>
          <input type="number" bind:value={amountTenge} min="1" step="1" placeholder="0" />
          {#if amountTiyin > 0}
            <div style="font-size:0.6rem;letter-spacing:0.15em;color:var(--muted-color)">
              = {amountTiyin.toLocaleString()} ТЫЙЫН
              {#if amountTiyin > account.balance}
                <span style="color:#ef4444;margin-left:8px">⚠ НЕДОСТАТОЧНО СРЕДСТВ</span>
              {/if}
            </div>
          {/if}
        </div>

        <button
          onclick={handleTransfer}
          disabled={!canTransfer}
          class="btn-fill"
          style="font-family:'Share Tech Mono',monospace;font-size:0.75rem;letter-spacing:0.25em;border:1px solid var(--accent-color);color:var(--text-color);padding:12px;cursor:pointer;background:transparent;{!canTransfer ? 'opacity:0.3;cursor:not-allowed' : ''}"
        >
          {transferring ? '[ ОТПРАВКА... ]' : '[ ПЕРЕВЕСТИ ]'}
        </button>
      </div>

    {/if}
  </div>
</div>