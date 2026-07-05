package xyz.wolegelei.nepu_fams.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.Result;
import xyz.wolegelei.nepu_fams.entity.College;
import xyz.wolegelei.nepu_fams.service.CollegeService;
import xyz.wolegelei.nepu_fams.vo.CollegeVO;

import java.util.List;

@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping("/list")
    public Result<List<CollegeVO>> listAll() {
        List<CollegeVO> list = collegeService.listAll();
        return Result.success(list);
    }

    @GetMapping("/all")
    public Result<List<CollegeVO>> all() {
        List<CollegeVO> list = collegeService.listAll();
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> addCollege(@RequestBody College college) {
        collegeService.addCollege(college);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateCollege(@PathVariable Long id, @RequestBody College college) {
        collegeService.updateCollege(id, college);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return Result.success();
    }
}
