package com.neighbor.controller.api;

import com.neighbor.common.api.ApiResponse;
import com.neighbor.dto.CategoryDTO;
import com.neighbor.dto.CommunityDTO;
import com.neighbor.entity.Community;
import com.neighbor.entity.Category;
import com.neighbor.repository.CommunityRepository;
import com.neighbor.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
    @Cacheable(value = "communities")
    public ApiResponse<List<CommunityDTO>> getCommunities() {
        List<Community> communities = communityRepository.findAll();
        List<CommunityDTO> dtos = communities.stream()
                .map(community -> new CommunityDTO(
                        community.getId(),
                        community.getName(),
                        community.getAddress()
                ))
                .collect(Collectors.toList());
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/categories")
    @Cacheable(value = "categories")
    public ApiResponse<List<CategoryDTO>> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> dtos = categories.stream()
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getIcon(),
                        category.getSortOrder()
                ))
                .collect(Collectors.toList());
        return ApiResponse.ok(dtos);
    }

    @GetMapping("/stats/carbon")
    public ApiResponse<CarbonStats> getCarbonStats() {
        return ApiResponse.ok(new CarbonStats(125000, 36500));
    }

    public record CarbonStats(
            int totalCo2Saved,
            int todayCo2Saved
    ) {}
}
