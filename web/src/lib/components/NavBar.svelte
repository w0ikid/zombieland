<script lang="ts">
  import { page } from '$app/stores';
  import { userManager, hasRole } from '$lib/auth';
  import { onMount } from 'svelte';

  import ThemeToggle from './ThemeToggle.svelte';

  let isSupport = $state(false);

  onMount(async () => {
    const user = await userManager.getUser();
    isSupport = hasRole(user, 'support') || hasRole(user, 'admin');
  });

  function isActive(href: string) {
    return $page.url.pathname === href;
  }
</script>

<nav style="
  font-family:'Share Tech Mono',monospace;
  position:fixed;bottom:0;left:0;right:0;z-index:9999;
  background:var(--bg-color);
  border-top:1px solid var(--border-color);
  display:flex;justify-content:center;
  align-items:center;
">
  <div style="display:flex;flex:1;justify-content:center;margin-left:40px">
    <a href="/districts" style="
      font-size:0.65rem;letter-spacing:0.25em;padding:14px 24px;
      text-decoration:none;
      color:{isActive('/districts') ? 'var(--text-color)' : 'var(--muted-color)'};
      border-top:2px solid {isActive('/districts') ? 'var(--accent-color)' : 'transparent'};
    ">КАРТА</a>

    <a href="/wallet" style="
      font-size:0.65rem;letter-spacing:0.25em;padding:14px 24px;
      text-decoration:none;
      color:{isActive('/wallet') ? 'var(--text-color)' : 'var(--muted-color)'};
      border-top:2px solid {isActive('/wallet') ? 'var(--accent-color)' : 'transparent'};
    ">КОШЕЛЁК</a>

    {#if isSupport}
      <a href="/admin" style="
        font-size:0.65rem;letter-spacing:0.25em;padding:14px 24px;
        text-decoration:none;
        color:{isActive('/admin') ? 'var(--text-color)' : 'var(--muted-color)'};
        border-top:2px solid {isActive('/admin') ? 'var(--accent-color)' : 'transparent'};
      ">ADMIN</a>
    {/if}
  </div>

  <div style="padding:0 20px">
    <ThemeToggle />
  </div>
</nav>