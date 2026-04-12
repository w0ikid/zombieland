import { userManager } from '$lib/auth';

export async function getAuthHeaders(): Promise<HeadersInit> {
  const user = await userManager.getUser();
  return {
    'Authorization': `Bearer ${user?.access_token ?? ''}`,
    'Content-Type': 'application/json',
  };
}

export async function apiFetch(url: string, init?: RequestInit): Promise<Response> {
  const headers = await getAuthHeaders();
  return fetch(url, {
    ...init,
    headers: {
      ...headers,
      ...init?.headers,
    },
  });
}
