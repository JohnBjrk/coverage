import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ListV2DTO } from "./data.model";

@Injectable({
  providedIn: 'root'
})
export class PlayWhatService {

  images = ["https://i.scdn.co/image/b38591beaa29b5c54b8376ef01534de403f4414d"];

  constructor(private http: HttpClient) { }

  getImages(): Observable<string[]> {
    // return this.images
    return this.http.get<string[]>("/list");
  }

  getPlayingData(): Observable<ListV2DTO> {
    return this.http.get<ListV2DTO>("/list_v2")
  }
}
