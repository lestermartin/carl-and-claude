import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login.component';
import { ShellComponent } from './pages/shell.component';
import { HomeComponent } from './pages/home.component';
import { ProfileComponent } from './pages/profile.component';
import { TradeComponent } from './pages/trade.component';
import { TransactionsComponent } from './pages/transactions.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      { path: 'home', component: HomeComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'trade', component: TradeComponent },
      { path: 'transactions', component: TransactionsComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
