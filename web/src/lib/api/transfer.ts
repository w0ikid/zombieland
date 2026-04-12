// api/transfer.ts
import { apiFetch } from './client';

export async function fundDistrict(
  toAccountNumber: string,
  amount: number,
  districtId: number
) {
  const res = await apiFetch('/api/v1/transfers', {
    method: 'POST',
    body: JSON.stringify({
      to_account_number: toAccountNumber,
      amount,
      currency: 'KZT',
      idempotency_key: `district-${districtId}-${Date.now()}`,
    }),
  });
  return res.json(); // TransactionResponse
}