package com.neighbor.controller.api;

import com.neighbor.common.api.ApiResponse;
import com.neighbor.entity.Community;
import com.neighbor.entity.Category;
import com.neighbor.repository.CommunityRepository;
import com.neighbor.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicController {

    private final CommunityRepository communityRepository;
    private final CategoryRepository categoryRepository;

    public PublicController(CommunityRepository communityRepository, CategoryRepository categoryRepository) {
        this.communityRepository = communityRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/communities")
    public ApiResponse<List<Community>> getCommunities() {
        return ApiResponse.ok(communityRepository.findAll());
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories() {
        return ApiResponse.ok(categoryRepository.findAll());
    }

    @GetMapping("/stats/carbon")
    public ApiResponse<CarbonStats> getCarbonStats() {
        // 这里可以根据实际数据计算碳排放
        return ApiResponse.ok(new CarbonStats(125000, 36500));
    }

    public record CarbonStats(
            int totalCo2Saved,  // 总碳减排量（克）
            int todayCo2Saved   // 今日碳减排量（克）
    ) {}
}
