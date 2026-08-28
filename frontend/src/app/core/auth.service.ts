import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from './models';

const TOKEN_KEY = 'trading-app.token';
const NAME_KEY = 'trading-app.displayName';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  readonly displayName = signal<string | null>(localStorage.getItem(NAME_KEY));
  readonly isAuthenticated = computed(() => this.token() !== null);

  get bearer(): string | null {
    return this.token();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.apiBase}/auth/login`, { username, password })
      .pipe(
        tap((res) => {
          localStorage.setItem(TOKEN_KEY, res.token);
          localStorage.setItem(NAME_KEY, res.displayName);
          this.token.set(res.token);
          this.displayName.set(res.displayName);
        }),
      );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(NAME_KEY);
    this.token.set(null);
    this.displayName.set(null);
    void this.router.navigate(['/login']);
  }
}
