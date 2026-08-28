import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ApiService } from '../core/api.service';
import { Transaction } from '../core/models';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, DecimalPipe],
  template: `
    <h1>Transaction log</h1>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <p class="error">{{ error() }}</p>
    } @else {
      <div class="panel table-scroll">
        <table>
          <thead>
            <tr>
              <th class="left">Date</th>
              <th class="left">Order</th>
              <th class="left">Symbol</th>
              <th>Qty</th>
              <th>Limit</th>
              <th>Executed</th>
              <th>Cash change</th>
              <th class="left">Status</th>
            </tr>
          </thead>
          <tbody>
            @for (t of transactions(); track t.id) {
              <tr>
                <td class="left">{{ t.createdAt | date: 'medium' }}</td>
                <td class="left">{{ t.side }} / {{ t.orderType }}</td>
                <td class="left">{{ t.symbol }} <span class="muted">{{ t.exchangeCode }}</span></td>
                <td>{{ t.quantity | number }}</td>
                <td>{{ t.limitPriceUsd !== null ? (t.limitPriceUsd | currency: 'USD') : '—' }}</td>
                <td>{{ t.executedPriceUsd !== null ? (t.executedPriceUsd | currency: 'USD') : '—' }}</td>
                <td [class.pos]="t.cashDeltaUsd > 0" [class.neg]="t.cashDeltaUsd < 0">
                  {{ t.cashDeltaUsd | currency: 'USD' }}
                </td>
                <td class="left">
                  <span class="badge" [class.filled]="t.status === 'FILLED'" [class.rejected]="t.status === 'REJECTED'">
                    {{ t.status }}
                  </span>
                  @if (t.reason) {
                    <div class="muted" style="font-weight: 400">{{ t.reason }}</div>
                  }
                </td>
              </tr>
            } @empty {
              <tr>
                <td class="left muted" colspan="8">No transactions yet.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class TransactionsComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly transactions = signal<Transaction[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.getTransactions().subscribe({
      next: (list) => {
        this.transactions.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your transactions.');
        this.loading.set(false);
      },
    });
  }
}
