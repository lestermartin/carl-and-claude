import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Portfolio } from '../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, RouterLink],
  template: `
    <h1>Portfolio</h1>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
    } @else if (portfolio(); as p) {
      <div class="grid-kpi" style="margin-bottom: 1.5rem">
        <div class="panel kpi">
          <label>Cash balance</label>
          <div class="value">{{ p.cashBalanceUsd | currency: 'USD' }}</div>
        </div>
        <div class="panel kpi">
          <label>Holdings value</label>
          <div class="value">{{ p.holdingsMarketValueUsd | currency: 'USD' }}</div>
        </div>
        <div class="panel kpi">
          <label>Total account value</label>
          <div class="value">{{ p.totalAccountValueUsd | currency: 'USD' }}</div>
        </div>
        <div class="panel kpi">
          <label>Unrealized P/L</label>
          <div class="value" [class.pos]="p.totalUnrealizedPlUsd >= 0" [class.neg]="p.totalUnrealizedPlUsd < 0">
            {{ p.totalUnrealizedPlUsd | currency: 'USD' }}
          </div>
        </div>
      </div>

      <div class="panel">
        <h2>Holdings ({{ p.holdings.length }})</h2>
        @if (p.holdings.length === 0) {
          <p class="muted">No holdings yet. <a routerLink="/trade">Place your first order →</a></p>
        } @else {
          <div class="table-scroll">
            <table>
              <thead>
                <tr>
                  <th class="left">Symbol</th>
                  <th class="left">Exchange</th>
                  <th>Qty</th>
                  <th>Avg cost</th>
                  <th>Price</th>
                  <th>Market value</th>
                  <th>Unrealized P/L</th>
                </tr>
              </thead>
              <tbody>
                @for (h of p.holdings; track h.symbol) {
                  <tr>
                    <td class="left">
                      <strong>{{ h.symbol }}</strong>
                      @if (h.exchangeOpen) {
                        <a
                          class="sell-tag"
                          routerLink="/trade"
                          [queryParams]="{
                            symbol: h.symbol,
                            exchange: h.exchangeCode,
                            side: 'SELL',
                            orderType: 'MARKET',
                            quantity: h.quantity
                          }"
                          [title]="'Sell ' + h.symbol"
                        >
                          <svg class="sell-tag__img" viewBox="0 0 30 12" width="30" height="12" role="img" aria-label="sell">
                            <rect x="0.5" y="0.5" width="29" height="11" rx="5.5" />
                            <text x="15" y="8.7" text-anchor="middle">sell</text>
                          </svg>
                        </a>
                      } @else {
                        <span class="sell-tag disabled" [title]="h.exchangeCode + ' is closed for trading'">
                          <svg class="sell-tag__img" viewBox="0 0 30 12" width="30" height="12" role="img" aria-label="sell">
                            <rect x="0.5" y="0.5" width="29" height="11" rx="5.5" />
                            <text x="15" y="8.7" text-anchor="middle">sell</text>
                          </svg>
                        </span>
                      }
                      <div class="muted" style="font-weight: 400">{{ h.companyName }}</div>
                    </td>
                    <td class="left">
                      {{ h.exchangeCode }}
                      <span
                        class="mkt-dot"
                        [class.open]="h.exchangeOpen"
                        [class.closed]="!h.exchangeOpen"
                        [title]="h.exchangeCode + (h.exchangeOpen ? ' is open for trading' : ' is closed')"
                        >{{ h.exchangeOpen ? '●' : '○' }}</span
                      >
                    </td>
                    <td>{{ h.quantity | number }}</td>
                    <td>{{ h.avgCostBasisUsd | currency: 'USD' }}</td>
                    <td>{{ h.priceUsd | currency: 'USD' }}</td>
                    <td>{{ h.marketValueUsd | currency: 'USD' }}</td>
                    <td [class.pos]="h.unrealizedPlUsd >= 0" [class.neg]="h.unrealizedPlUsd < 0">
                      {{ h.unrealizedPlUsd | currency: 'USD' }}
                      <span class="muted">({{ h.unrealizedPlPct | number: '1.2-2' }}%)</span>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    }
  `,
})
export class HomeComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly portfolio = signal<Portfolio | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getPortfolio().subscribe({
      next: (p) => {
        this.portfolio.set(p);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your portfolio.');
        this.loading.set(false);
      },
    });
  }
}
