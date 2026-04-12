package kz.kbtu.yarmaq.zombiemanager.service;

import kz.kbtu.yarmaq.zombiemanager.dto.SortieOutcome;

public interface SortieService {
    SortieOutcome play(Integer districtId, String userAction);
}
