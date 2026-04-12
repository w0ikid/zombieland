<script lang="ts">
  import { userManager } from '$lib/auth';
  import { page } from '$app/stores';
  import { onMount } from 'svelte';
  import './layout.css';
  import Navbar from '$lib/components/NavBar.svelte';

  let { children } = $props();

  const hideNav = $derived(
    $page.url.pathname === '/callback'
  );

  onMount(async () => {
    if ($page.url.pathname === '/callback') return;
    const user = await userManager.getUser();
    if (!user || user.expired) {
      await userManager.signinRedirect();
    }
  });
</script>

{@render children()}
{#if !hideNav}
  <Navbar />
{/if}