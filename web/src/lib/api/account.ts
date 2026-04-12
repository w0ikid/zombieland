// api/account.ts
import { apiFetch } from './client';

export async function getMyAccount(userId: string, currency = 'KZT') {
  const res = await apiFetch(
    `/api/v1/accounts/by-user-currency?user_id=${userId}&currency=${currency}`
  );
  return res.json();
}