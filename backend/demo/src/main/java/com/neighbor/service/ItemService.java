package com.neighbor.service;

import com.neighbor.dto.ItemDetailDTO;
import com.neighbor.dto.ItemListDTO;
import com.neighbor.entity.Item;
import com.neighbor.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ItemService {
    Page<ItemListDTO> getItems(List<ItemStatus> statuses, Long communityId, Long categoryId, Pageable pageable);
    Page<ItemListDTO> searchItems(String keyword, List<ItemStatus> statuses, Pageable pageable);
    ItemDetailDTO getItemById(Long id);
    Item createItem(Map<String, Object> itemData, Long userId);
    Item updateItem(Long id, Map<String, Object> itemData, Long userId);
    void withdrawItem(Long id, Long userId);
    Item saveDraft(Map<String, Object> itemData, Long userId);
    List<ItemListDTO> getSimilarItems(Long itemId, int limit);
    void deleteItem(Long id, Long userId);
}
