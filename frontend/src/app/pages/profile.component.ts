import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { Profile } from '../core/models';

interface ProfileForm {
  firstName: string;
  lastName: string;
  taxId: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  postalCode: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1>Edit profile</h1>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (form(); as m) {
      <form class="panel stack" (ngSubmit)="save()">
        <div>
          <label>Username</label>
          <input [value]="username()" disabled />
          <p class="muted" style="font-size: 0.8rem">Username and password cannot be changed.</p>
        </div>
        <div class="row">
          <div>
            <label for="firstName">First name</label>
            <input id="firstName" name="firstName" [(ngModel)]="m.firstName" required />
          </div>
          <div>
            <label for="lastName">Last name</label>
            <input id="lastName" name="lastName" [(ngModel)]="m.lastName" required />
          </div>
        </div>
        <div>
          <label for="taxId">Tax ID (SSN)</label>
          <input id="taxId" name="taxId" [(ngModel)]="m.taxId" placeholder="123-45-6789" required />
        </div>
        <div>
          <label for="addressLine1">Address line 1</label>
          <input id="addressLine1" name="addressLine1" [(ngModel)]="m.addressLine1" required />
        </div>
        <div>
          <label for="addressLine2">Address line 2</label>
          <input id="addressLine2" name="addressLine2" [(ngModel)]="m.addressLine2" />
        </div>
        <div class="row">
          <div>
            <label for="city">City</label>
            <input id="city" name="city" [(ngModel)]="m.city" required />
          </div>
          <div>
            <label for="state">State</label>
            <input id="state" name="state" [(ngModel)]="m.state" maxlength="2" placeholder="NY" required />
          </div>
          <div>
            <label for="postalCode">ZIP</label>
            <input id="postalCode" name="postalCode" [(ngModel)]="m.postalCode" placeholder="10001" required />
          </div>
        </div>

        @if (message()) {
          <p [class.error]="isError()" [class.pos]="!isError()">{{ message() }}</p>
        }
        <div>
          <button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Save changes' }}</button>
        </div>
      </form>
    }
  `,
})
export class ProfileComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly form = signal<ProfileForm | null>(null);
  readonly username = signal('');
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly message = signal<string | null>(null);
  readonly isError = signal(false);

  ngOnInit(): void {
    this.api.getProfile().subscribe({
      next: (p) => this.apply(p),
      error: () => {
        this.loading.set(false);
        this.message.set('Could not load your profile.');
        this.isError.set(true);
      },
    });
  }

  private apply(p: Profile): void {
    this.username.set(p.username);
    this.form.set({
      firstName: p.firstName,
      lastName: p.lastName,
      taxId: p.taxId,
      addressLine1: p.addressLine1,
      addressLine2: p.addressLine2 ?? '',
      city: p.city,
      state: p.state,
      postalCode: p.postalCode,
    });
    this.loading.set(false);
  }

  save(): void {
    const m = this.form();
    if (!m) {
      return;
    }
    this.saving.set(true);
    this.message.set(null);
    this.api
      .updateProfile({
        firstName: m.firstName.trim(),
        lastName: m.lastName.trim(),
        taxId: m.taxId.trim(),
        addressLine1: m.addressLine1.trim(),
        addressLine2: m.addressLine2.trim() ? m.addressLine2.trim() : null,
        city: m.city.trim(),
        state: m.state.trim().toUpperCase(),
        postalCode: m.postalCode.trim(),
      })
      .subscribe({
        next: (p) => {
          this.apply(p);
          this.saving.set(false);
          this.isError.set(false);
          this.message.set('Profile saved.');
        },
        error: (err: { error?: { detail?: string } }) => {
          this.saving.set(false);
          this.isError.set(true);
          this.message.set(err?.error?.detail ?? 'Save failed. Check the field formats and try again.');
        },
      });
  }
}
