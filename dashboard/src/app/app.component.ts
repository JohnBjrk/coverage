import { Component, OnInit } from '@angular/core';
import { PlayWhatService } from "./play-what.service";
import { interval } from "rxjs";
import { startWith, switchMap } from "rxjs/operators";

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
    interval(5000)
      .pipe(
        startWith(0),
        switchMap(() => this.playWhatService.getImages() )
      )
      .subscribe(images => this.images = images);
    //this.playWhatService.getImages().subscribe(images => this.images = images);
  }
}
