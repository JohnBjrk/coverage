import { Component, OnInit } from '@angular/core';
import { PlayWhatService } from "./play-what.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'dashboard';

  images = Array<string>();

  constructor(private playWhatService: PlayWhatService) {

  }

  ngOnInit() {
    this.playWhatService.getImages().subscribe(images => this.images = images);
  }
}
