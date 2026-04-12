// $lib/money.ts

export const toTenge = (tiyin: number) => (tiyin / 100).toFixed(2);
export const toTiyin = (tenge: number) => Math.round(tenge * 100);

export const formatTenge = (tiyin: number) => `${toTenge(tiyin)} ₸`;