import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Exchange, OrderResult, OrderType, Security, Side } from '../core/models';

@Component({
  selector: 'app-trade',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, RouterLink],
  template: `
    <h1>Place an order</h1>

    <form class="panel stack" (ngSubmit)="submit()">
      <div class="row">
        <div>
          <label for="exchange">Exchange</label>
          <select id="exchange" name="exchange" [(ngModel)]="exchange" (ngModelChange)="onExchangeChange()">
            @for (ex of exchanges(); track ex.code) {
              <option [value]="ex.code">{{ ex.name }}</option>
            }
          </select>
        </div>
        <div>
          <label for="symbol">Security</label>
          <select id="symbol" name="symbol" [(ngModel)]="symbol" [disabled]="securities().length === 0">
            @for (s of securities(); track s.symbol) {
              <option [value]="s.symbol">{{ s.symbol }} — {{ s.companyName }}</option>
            }
          </select>
        </div>
      </div>

      @if (selected(); as s) {
        <p class="muted">Current price: <strong>{{ s.priceUsd | currency: 'USD' }}</strong> (USD)</p>
      }

      <div class="row">
        <div>
          <label for="side">Side</label>
          <select id="side" name="side" [(ngModel)]="side">
            <option value="BUY">Buy</option>
            <option value="SELL">Sell</option>
          </select>
        </div>
        <div>
          <label for="orderType">Order type</label>
          <select id="orderType" name="orderType" [(ngModel)]="orderType">
            <option value="MARKET">Market</option>
            <option value="LIMIT">Limit</option>
          </select>
        </div>
        <div>
          <label for="quantity">Quantity (shares)</label>
          <input id="quantity" name="quantity" type="number" min="1" step="1" [(ngModel)]="quantity" />
        </div>
        @if (orderType === 'LIMIT') {
          <div>
            <label for="limitPrice">Limit price (USD)</label>
            <input id="limitPrice" name="limitPrice" type="number" min="0.01" step="0.01" [(ngModel)]="limitPrice" />
          </div>
        }
      </div>

      @if (estimate(); as est) {
        <p class="muted">Estimated {{ side === 'BUY' ? 'cost' : 'proceeds' }}: <strong>{{ est | currency: 'USD' }}</strong></p>
      }

      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
      <div>
        <button type="submit" [disabled]="submitting() || !symbol || quantity < 1">
          {{ submitting() ? 'Submitting…' : 'Submit order' }}
        </button>
      </div>
    </form>

    @if (result(); as r) {
      <div class="panel stack" style="margin-top: 1.5rem">
        <div>
          <span class="badge" [class.filled]="r.status === 'FILLED'" [class.rejected]="r.status === 'REJECTED'">
            {{ r.status }}
          </span>
        </div>
        <div class="table-scroll">
          <table>
            <tbody>
              <tr><td class="left">Order</td><td class="left">{{ r.side }} {{ r.quantity }} {{ r.symbol }} ({{ r.orderType }})</td></tr>
              @if (r.limitPriceUsd !== null) {
                <tr><td class="left">Limit price</td><td class="left">{{ r.limitPriceUsd | currency: 'USD' }}</td></tr>
              }
              @if (r.executedPriceUsd !== null) {
                <tr><td class="left">Executed price</td><td class="left">{{ r.executedPriceUsd | currency: 'USD' }}</td></tr>
              }
              <tr><td class="left">Cash change</td><td class="left">{{ r.cashDeltaUsd | currency: 'USD' }}</td></tr>
              <tr><td class="left">New cash balance</td><td class="left">{{ r.newCashBalanceUsd | currency: 'USD' }}</td></tr>
              @if (r.reason) {
                <tr><td class="left">Reason</td><td class="left">{{ r.reason }}</td></tr>
              }
            </tbody>
          </table>
        </div>
        <p class="muted">
          See it on your <a routerLink="/home">portfolio</a> or in the
          <a routerLink="/transactions">transaction log</a>.
        </p>
      </div>
    }
  `,
})
export class TradeComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly exchanges = signal<Exchange[]>([]);
  readonly securities = signal<Security[]>([]);

  exchange = '';
  symbol = '';
  side: Side = 'BUY';
  orderType: OrderType = 'MARKET';
  quantity = 1;
  limitPrice: number | null = null;

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<OrderResult | null>(null);

  selected(): Security | null {
    return this.securities().find((s) => s.symbol === this.symbol) ?? null;
  }

  estimate(): number | null {
    const s = this.selected();
    return s && this.quantity > 0 ? s.priceUsd * this.quantity : null;
  }

  ngOnInit(): void {
    this.api.getExchanges().subscribe((list) => {
      this.exchanges.set(list);
      if (list.length > 0) {
        this.exchange = list[0].code;
        this.onExchangeChange();
      }
    });
  }

  onExchangeChange(): void {
    this.symbol = '';
    this.securities.set([]);
    if (!this.exchange) {
      return;
    }
    this.api.getSecurities(this.exchange).subscribe((list) => {
      this.securities.set(list);
      if (list.length > 0) {
        this.symbol = list[0].symbol;
      }
    });
  }

  submit(): void {
    if (!this.symbol || this.quantity < 1) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.api
      .placeOrder({
        symbol: this.symbol,
        side: this.side,
        orderType: this.orderType,
        quantity: Math.trunc(this.quantity),
        limitPriceUsd: this.orderType === 'LIMIT' ? this.limitPrice : null,
      })
      .subscribe({
        next: (r) => {
          this.submitting.set(false);
          this.result.set(r);
        },
        error: (err: { error?: { detail?: string } }) => {
          this.submitting.set(false);
          this.error.set(err?.error?.detail ?? 'Order could not be submitted.');
        },
      });
  }
}
