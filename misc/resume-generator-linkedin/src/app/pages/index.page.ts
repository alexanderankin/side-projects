import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './index.page.html',
  styleUrls: ['./index.page.css'],
})
export default class HomeComponent {
  count = 0;

  increment() {
    this.count++;
  }
}
