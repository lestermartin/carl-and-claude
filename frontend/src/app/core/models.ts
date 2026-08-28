export interface LoginResponse {
  token: string;
  username: string;
  displayName: string;
}

export interface Profile {
  username: string;
  firstName: string;
  lastName: string;
  taxId: string;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  state: string;
  postalCode: string;
  cashBalanceUsd: number;
}

export type UpdateProfile = Omit<Profile, 'username' | 'cashBalanceUsd'>;

export interface Exchange {
  code: string;
  name: string;
  timeZone: string | null;
  openLocal: string | null;
  closeLocal: string | null;
  openDays: string | null;
  open: boolean;
}

export interface Security {
  symbol: string;
  companyName: string;
  exchangeCode: string;
  currencyNative: string;
  priceUsd: number;
}

export interface Holding {
  symbol: string;
  exchangeCode: string;
  companyName: string;
  quantity: number;
  avgCostBasisUsd: number;
  priceUsd: number;
  marketValueUsd: number;
  costBasisUsd: number;
  unrealizedPlUsd: number;
  unrealizedPlPct: number;
  exchangeOpen: boolean;
}

export interface Portfolio {
  cashBalanceUsd: number;
  holdingsMarketValueUsd: number;
  totalAccountValueUsd: number;
  totalCostBasisUsd: number;
  totalUnrealizedPlUsd: number;
  holdings: Holding[];
}

export type Side = 'BUY' | 'SELL';
export type OrderType = 'MARKET' | 'LIMIT';
export type OrderStatus = 'FILLED' | 'REJECTED';

export interface OrderRequest {
  symbol: string;
  side: Side;
  orderType: OrderType;
  quantity: number;
  limitPriceUsd?: number | null;
}

export interface OrderResult {
  status: OrderStatus;
  side: Side;
  orderType: OrderType;
  symbol: string;
  exchangeCode: string;
  quantity: number;
  limitPriceUsd: number | null;
  executedPriceUsd: number | null;
  cashDeltaUsd: number;
  newCashBalanceUsd: number;
  reason: string | null;
  createdAt: string;
}

export interface Transaction {
  id: number;
  side: Side;
  orderType: OrderType;
  status: OrderStatus;
  symbol: string;
  exchangeCode: string;
  quantity: number;
  limitPriceUsd: number | null;
  executedPriceUsd: number | null;
  cashDeltaUsd: number;
  reason: string | null;
  createdAt: string;
}
