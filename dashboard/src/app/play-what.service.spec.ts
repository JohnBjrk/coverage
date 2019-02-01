import { TestBed } from '@angular/core/testing';

import { PlayWhatService } from './play-what.service';

describe('PlayWhatService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: PlayWhatService = TestBed.get(PlayWhatService);
    expect(service).toBeTruthy();
  });
});
