import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="centered">
      <form class="panel stack" style="width: 22rem" (ngSubmit)="submit()">
        <h1>📈 Carl's Brokerage</h1>
        <div>
          <label for="username">Username</label>
          <input id="username" name="username" [(ngModel)]="username" autocomplete="username" required />
        </div>
        <div>
          <label for="password">Password</label>
          <input
            id="password"
            name="password"
            type="password"
            [(ngModel)]="password"
            autocomplete="current-password"
            required
          />
        </div>
        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
        <button type="submit" [disabled]="loading()">
          {{ loading() ? 'Signing in…' : 'Sign in' }}
        </button>
        <p class="muted" style="font-size: 0.8rem">
          Demo accounts: <code>customer1</code> … <code>customer9</code> / <code>cu$tP@$$w0rd</code>
        </p>
      </form>
    </div>
  `,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  password = '';
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  submit(): void {
    if (!this.username || !this.password) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/home']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(err.status === 401 ? 'Invalid username or password.' : 'Login failed. Try again.');
      },
    });
  }
}
