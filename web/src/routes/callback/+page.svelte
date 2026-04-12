<script lang="ts">
  import { userManager } from '$lib/auth';
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  let error = $state('');

  onMount(async () => {
    try {
      await userManager.signinRedirectCallback();
      goto('/districts');
    } catch (e) {
      console.error(e);
      error = String(e);
    }
  });
</script>

{#if error}
  <p>Ошибка: {error}</p>
{:else}
  <p>Загрузка...</p>
{/if}