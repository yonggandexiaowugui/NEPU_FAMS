package xyz.wolegelei.nepu_fams.service;

import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.vo.CollegeVO;

import java.util.List;

public interface CollegeService {

    List<CollegeVO> listAll();

    CollegeVO getById(Long id);

    void addCollege(College college);

    void updateCollege(Long id, College college);

    void deleteCollege(Long id);
}
