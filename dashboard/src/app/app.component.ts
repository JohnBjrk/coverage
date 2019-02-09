import { Component, OnInit } from '@angular/core';
import { PlayWhatService } from "./play-what.service";
import { interval } from "rxjs";
import { startWith, switchMap } from "rxjs/operators";
import { ActivatedRoute } from "@angular/router";
import { PlayingData } from "./data.model";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'dashboard';

  loggedin: boolean = false;
  polling: boolean = false;
  playingDataList = Array<PlayingData>();

  constructor(private playWhatService: PlayWhatService, private route: ActivatedRoute) {

  }

  ngOnInit() {
    this.route.queryParamMap.subscribe(queryParams => {
      this.loggedin = queryParams.get("loggedin") == "yes";
      if (this.loggedin && !this.polling) {
        interval(1000)
          .pipe(
            startWith(0),
            switchMap(() => this.playWhatService.getPlayingData())
          ).subscribe(listV2DTO => this.playingDataList = listV2DTO.playing_data_list);
        this.polling = true;
      }
    })

  }
}
