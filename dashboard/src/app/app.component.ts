import { Component, OnInit } from '@angular/core';
import { PlayWhatService } from "./play-what.service";
import { interval } from "rxjs";
import { startWith, switchMap } from "rxjs/operators";
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'dashboard';

  loggedin: boolean = false;
  polling: boolean = false;
  images = Array<string>();

  constructor(private playWhatService: PlayWhatService, private route: ActivatedRoute) {

  }

  ngOnInit() {
    this.route.queryParamMap.subscribe(queryParams => {
      this.loggedin = queryParams.get("loggedin") == "yes";
      if (this.loggedin && !this.polling) {
        interval(5000)
          .pipe(
            startWith(0),
            switchMap(() => this.playWhatService.getImages())
          )
          //.subscribe(images => this.images = [images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0], images[0]]);
          .subscribe(images => this.images = images);
        this.polling = true;
      }
    })

  }
}
