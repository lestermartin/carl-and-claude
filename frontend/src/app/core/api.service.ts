import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Exchange,
  OrderRequest,
  OrderResult,
  Portfolio,
  Profile,
  Security,
  Transaction,
  UpdateProfile,
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBase;

  getProfile(): Observable<Profile> {
    return this.http.get<Profile>(`${this.base}/profile`);
  }

  updateProfile(body: UpdateProfile): Observable<Profile> {
    return this.http.put<Profile>(`${this.base}/profile`, body);
  }

  getExchanges(): Observable<Exchange[]> {
    return this.http.get<Exchange[]>(`${this.base}/exchanges`);
  }

  getSecurities(exchangeCode: string): Observable<Security[]> {
    return this.http.get<Security[]>(`${this.base}/securities`, {
      params: new HttpParams().set('exchange', exchangeCode),
    });
  }

  getPortfolio(): Observable<Portfolio> {
    return this.http.get<Portfolio>(`${this.base}/portfolio`);
  }

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.base}/transactions`);
  }

  placeOrder(body: OrderRequest): Observable<OrderResult> {
    return this.http.post<OrderResult>(`${this.base}/orders`, body);
  }
}
