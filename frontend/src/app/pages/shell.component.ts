import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="app-header">
      <strong>📈 Carl's Brokerage</strong>
      <nav>
        <a routerLink="/home" routerLinkActive="active">Home</a>
        <a routerLink="/trade" routerLinkActive="active">Trade</a>
        <a routerLink="/transactions" routerLinkActive="active">Transactions</a>
        <a routerLink="/profile" routerLinkActive="active">Profile</a>
      </nav>
      <span class="muted">{{ auth.displayName() }}</span>
      <button class="secondary" (click)="auth.logout()">Log out</button>
    </header>
    <main class="content">
      <router-outlet />
    </main>
  `,
})
export class ShellComponent {
  readonly auth = inject(AuthService);
}
