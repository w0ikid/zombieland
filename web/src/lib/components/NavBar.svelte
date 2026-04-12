<script lang="ts">
  import { page } from '$app/stores';
  import { userManager, hasRole } from '$lib/auth';
  import { onMount } from 'svelte';

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
  background:#0a0a0a;
  border-top:1px solid rgba(180,32,32,0.3);
  display:flex;justify-content:center;
">
  <a href="/districts" style="
    font-size:0.65rem;letter-spacing:0.25em;padding:14px 32px;
    text-decoration:none;
    color:{isActive('/districts') ? '#e8e0d0' : '#444'};
    border-top:2px solid {isActive('/districts') ? '#b42020' : 'transparent'};
  ">КАРТА</a>

  <a href="/wallet" style="
    font-size:0.65rem;letter-spacing:0.25em;padding:14px 32px;
    text-decoration:none;
    color:{isActive('/wallet') ? '#e8e0d0' : '#444'};
    border-top:2px solid {isActive('/wallet') ? '#b42020' : 'transparent'};
  ">КОШЕЛЁК</a>

  {#if isSupport}
    <a href="/admin" style="
      font-size:0.65rem;letter-spacing:0.25em;padding:14px 32px;
      text-decoration:none;
      color:{isActive('/admin') ? '#e8e0d0' : '#444'};
      border-top:2px solid {isActive('/admin') ? '#b42020' : 'transparent'};
    ">ADMIN</a>
  {/if}
</nav>