<script lang="ts">
  import { userManager } from '$lib/auth';

  async function login() {
    await userManager.signinRedirect();
  }
</script>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Share+Tech+Mono&display=swap');

  .font-bebas { font-family: 'Bebas Neue', sans-serif; }
  .font-mono-tech { font-family: 'Share Tech Mono', monospace; }

  .grid-bg {
    background-image:
      linear-gradient(rgba(180, 40, 40, 0.07) 1px, transparent 1px),
      linear-gradient(90deg, rgba(180, 40, 40, 0.07) 1px, transparent 1px);
    background-size: 48px 48px;
  }

  .glow {
    background: radial-gradient(circle, rgba(180, 30, 30, 0.2) 0%, transparent 70%);
    animation: pulse 4s ease-in-out infinite;
  }

  @keyframes pulse {
    0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
    50% { opacity: 1; transform: translate(-50%, -50%) scale(1.15); }
  }

  .blink { animation: blink 1.2s step-end infinite; }
  @keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
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
    background: #b42020;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    z-index: -1;
  }
  .btn-fill:hover::before { transform: translateX(0); }
  .btn-fill:hover { color: #0a0a0a; }

  .scanlines {
    background: repeating-linear-gradient(
      0deg, transparent, transparent 2px,
      rgba(0,0,0,0.03) 2px, rgba(0,0,0,0.03) 4px
    );
  }
</style>

<!-- Скан-линии -->
<div class="scanlines fixed inset-0 pointer-events-none z-50"></div>

<div class="font-mono-tech min-h-screen bg-[#0a0a0a] text-[#e8e0d0] flex flex-col relative overflow-hidden">

  <!-- Сетка фон -->
  <div class="grid-bg absolute inset-0 z-0"></div>

  <!-- Виньетка -->
  <div class="absolute inset-0 z-[1]" style="background: radial-gradient(ellipse at center, transparent 40%, #0a0a0a 100%)"></div>

  <!-- Красное свечение -->
  <div class="glow absolute w-[600px] h-[600px] z-[1] rounded-full"
       style="top: 35%; left: 50%; transform: translate(-50%, -50%)"></div>

  <!-- Угловые декорации -->
  <div class="absolute top-6 left-10 w-5 h-5 border-t border-l border-[#b42020] opacity-50 z-10"></div>
  <div class="absolute top-6 right-10 w-5 h-5 border-t border-r border-[#b42020] opacity-50 z-10"></div>
  <div class="absolute bottom-6 left-10 w-5 h-5 border-b border-l border-[#b42020] opacity-50 z-10"></div>
  <div class="absolute bottom-6 right-10 w-5 h-5 border-b border-r border-[#b42020] opacity-50 z-10"></div>

  <!-- Навбар -->
  <nav class="relative z-10 flex justify-between items-center px-10 py-5 border-b border-[#b42020]/30">
    <span class="font-bebas text-2xl tracking-widest text-[#b42020]">ZOMBIELAND</span>
    <span class="text-[0.65rem] tracking-widest text-[#555]">
      SYSTEM STATUS: <span class="blink text-[#b42020]">CRITICAL</span>
    </span>
  </nav>

  <!-- Основной контент -->
  <main class="relative z-10 flex-1 flex flex-col items-center justify-center text-center px-6 py-16">

    <div class="text-[0.65rem] tracking-[0.3em] text-[#b42020] border border-[#b42020]/40 px-4 py-1.5 mb-8 inline-block">
      ZONE CONTROL SYSTEM // CLASSIFIED
    </div>

    <h1 class="font-bebas text-[clamp(5rem,14vw,11rem)] leading-none tracking-wider mb-6"
        style="text-shadow: 0 0 80px rgba(180,40,40,0.35)">
      ZOMBIE<br><span class="text-[#b42020]">LAND</span>
    </h1>

    <p class="text-[0.75rem] tracking-[0.2em] text-[#666] max-w-sm leading-loose mb-10">
      УПРАВЛЯЙ РАЙОНАМИ. КОНТРОЛИРУЙ РЕСУРСЫ.<br />
      ВЫЖИВАЙ ЛЮБОЙ ЦЕНОЙ.
    </p>

    <button
      onclick={login}
      class="btn-fill font-mono-tech text-[0.8rem] tracking-[0.25em] px-10 py-4 border border-[#b42020] text-[#e8e0d0] cursor-pointer bg-transparent"
    >
      [ ВОЙТИ В СИСТЕМУ ]
    </button>

  </main>

  <!-- Статистика внизу -->
  <footer class="relative z-10 flex justify-center gap-16 px-10 py-6 border-t border-white/5">
    <div class="text-center">
      <div class="font-bebas text-4xl text-[#b42020]">Z-4</div>
      <div class="text-[0.6rem] tracking-widest text-[#444] mt-1">УРОВЕНЬ УГРОЗЫ</div>
    </div>
    <div class="text-center">
      <div class="font-bebas text-4xl text-[#b42020]">312</div>
      <div class="text-[0.6rem] tracking-widest text-[#444] mt-1">АКТИВНЫХ ЗОНЫ</div>
    </div>
    <div class="text-center">
      <div class="font-bebas text-4xl text-[#b42020]">17%</div>
      <div class="text-[0.6rem] tracking-widest text-[#444] mt-1">ВЫЖИВШИХ</div>
    </div>
  </footer>

</div>