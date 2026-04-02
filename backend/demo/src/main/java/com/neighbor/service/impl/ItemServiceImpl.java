package com.neighbor.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.dto.ItemDetailDTO;
import com.neighbor.dto.ItemListDTO;
import com.neighbor.dto.OwnerDTO;
import com.neighbor.entity.*;
import com.neighbor.enums.ErrorCode;
import com.neighbor.enums.ItemStatus;
import com.neighbor.repository.*;
import com.neighbor.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    public ItemServiceImpl(ItemRepository itemRepository, ItemImageRepository itemImageRepository, 
                         CategoryRepository categoryRepository, UserRepository userRepository, 
                         CommunityRepository communityRepository) {
        this.itemRepository = itemRepository;
        this.itemImageRepository = itemImageRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
    }

    @Override
    public Page<ItemListDTO> getItems(List<ItemStatus> statuses, Long communityId, Long categoryId, Pageable pageable) {
        Page<Item> items;
        if (communityId != null && categoryId != null) {
            items = itemRepository.findByStatusInAndCommunityIdAndCategoryId(statuses, communityId, categoryId, pageable);
        } else if (communityId != null) {
            items = itemRepository.findByStatusInAndCommunityId(statuses, communityId, pageable);
        } else if (categoryId != null) {
            items = itemRepository.findByStatusInAndCategoryId(statuses, categoryId, pageable);
        } else {
            items = itemRepository.findByStatusIn(statuses, pageable);
        }
        return items.map(this::convertToItemListDTO);
    }

    @Override
    public Page<ItemListDTO> searchItems(String keyword, List<ItemStatus> statuses, Pageable pageable) {
        Page<Item> items = itemRepository.searchByKeyword(keyword, statuses, pageable);
        return items.map(this::convertToItemListDTO);
    }

    @Override
    public ItemDetailDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        item.setViewCount(item.getViewCount() + 1);
        itemRepository.save(item);
        return convertToItemDetailDTO(item);
    }

    @Override
    public Item createItem(Map<String, Object> itemData, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Item item = new Item();
        populateItemFromData(item, itemData, user);
        item.setStatus(ItemStatus.AVAILABLE);
        item.setPublishedAt(LocalDateTime.now());
        
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long id, Map<String, Object> itemData, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        
        if (!item.getOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_ITEM);
        }
        
        populateItemFromData(item, itemData, item.getOwner());
        return itemRepository.save(item);
    }

    @Override
    public void withdrawItem(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        
        if (!item.getOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_ITEM);
        }
        
        item.setStatus(ItemStatus.OFFLINE);
        itemRepository.save(item);
    }

    @Override
    public Item saveDraft(Map<String, Object> itemData, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Item item = new Item();
        populateItemFromData(item, itemData, user);
        item.setStatus(ItemStatus.DRAFT);
        
        return itemRepository.save(item);
    }

    @Override
    public List<ItemListDTO> getSimilarItems(Long itemId, int limit) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        
        List<Item> similarItems = itemRepository.findSimilarItems(
                item.getCategory().getId(),
                ItemStatus.AVAILABLE,
                itemId,
                limit
        );
        
        List<ItemListDTO> result = new ArrayList<>();
        for (Item similarItem : similarItems) {
            result.add(convertToItemListDTO(similarItem));
        }
        return result;
    }

    private void populateItemFromData(Item item, Map<String, Object> data, User user) {
        if (data.containsKey("name")) {
            item.setName((String) data.get("name"));
        }
        if (data.containsKey("description")) {
            item.setDescription((String) data.get("description"));
        }
        if (data.containsKey("story")) {
            item.setStory((String) data.get("story"));
        }
        if (data.containsKey("categoryId")) {
            Long categoryId = Long.valueOf(data.get("categoryId").toString());
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            item.setCategory(category);
        }
        if (data.containsKey("pricePerDay")) {
            item.setPricePerDay(new BigDecimal(data.get("pricePerDay").toString()));
        }
        if (data.containsKey("deposit")) {
            item.setDeposit(new BigDecimal(data.get("deposit").toString()));
        }
        if (data.containsKey("creditRequired")) {
            item.setCreditRequired(new BigDecimal(data.get("creditRequired").toString()));
        }
        if (data.containsKey("returnRequirements")) {
            item.setReturnRequirements((String) data.get("returnRequirements"));
        }
        if (data.containsKey("building")) {
            item.setBuilding((String) data.get("building"));
        }
        if (data.containsKey("tags")) {
            item.setTags((String) data.get("tags"));
        }
        
        item.setOwner(user);
        if (user.getCommunity() != null) {
            item.setCommunity(user.getCommunity());
        }
    }

    private ItemListDTO convertToItemListDTO(Item item) {
        String mainImage = "";
        List<ItemImage> images = itemImageRepository.findByItemIdOrderBySortOrderAsc(item.getId());
        if (!images.isEmpty()) {
            mainImage = images.get(0).getUrl();
        }
        
        OwnerDTO owner = new OwnerDTO(
                item.getOwner().getId(),
                item.getOwner().getNickname(),
                item.getOwner().getAvatar()
        );
        
        String locationText = "";
        if (item.getCommunity() != null) {
            locationText = item.getCommunity().getName();
            if (item.getBuilding() != null && !item.getBuilding().isEmpty()) {
                locationText += " " + item.getBuilding();
            }
        }
        
        List<String> tags = parseTags(item.getTags());
        
        return new ItemListDTO(
                item.getId(),
                item.getName(),
                mainImage,
                item.getPricePerDay(),
                item.getStatus(),
                owner,
                locationText,
                tags,
                item.getBorrowCount(),
                item.getViewCount()
        );
    }
    
    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private ItemDetailDTO convertToItemDetailDTO(Item item) {
        String communityName = item.getCommunity() != null ? item.getCommunity().getName() : "";
        String categoryName = item.getCategory() != null ? item.getCategory().getName() : "";
        Long categoryId = item.getCategory() != null ? item.getCategory().getId() : null;
        
        List<String> images = new ArrayList<>();
        List<ItemImage> itemImages = itemImageRepository.findByItemIdOrderBySortOrderAsc(item.getId());
        for (ItemImage image : itemImages) {
            images.add(image.getUrl());
        }
        
        OwnerDTO owner = new OwnerDTO(
                item.getOwner().getId(),
                item.getOwner().getNickname(),
                item.getOwner().getAvatar()
        );
        
        List<String> returnRequirements = parseTags(item.getReturnRequirements());
        List<String> tags = parseTags(item.getTags());
        
        return new ItemDetailDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getStory(),
                categoryId,
                categoryName,
                item.getPricePerDay(),
                item.getDeposit(),
                item.getCreditRequired(),
                returnRequirements,
                item.getStatus(),
                item.getBorrowCount(),
                item.getViewCount(),
                item.getFavoriteCount(),
                tags,
                owner,
                communityName,
                item.getBuilding(),
                item.getCreatedAt(),
                item.getPublishedAt(),
                images
        );
    }
}
