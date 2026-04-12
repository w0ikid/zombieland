import type { ResourceDTO } from './types';
import { apiFetch } from './client';

const BASE = '/api/v1/districts';

export async function getResources(districtId: number): Promise<ResourceDTO[]> {
  const res = await apiFetch(`${BASE}/${districtId}/resources`);
  return res.json();
}

export async function addResource(districtId: number, data: Omit<ResourceDTO, 'id'>): Promise<ResourceDTO> {
  const res = await apiFetch(`${BASE}/${districtId}/resources`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
  return res.json();
}