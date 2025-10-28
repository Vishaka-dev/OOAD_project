package com.ooadproject.backend.services;

import com.ooadproject.backend.dto.AddPersonalizedToCartRequest;
import com.ooadproject.backend.dto.PersonalizationDTO;
import com.ooadproject.backend.dto.PersonalizationOptionDTO;
import com.ooadproject.backend.entities.CartItem;
import com.ooadproject.backend.entities.PersonalizationOption;
import com.ooadproject.backend.entities.Product;
import com.ooadproject.backend.entities.User;
import com.ooadproject.backend.repositories.PersonalizationOptionRepository;
import com.ooadproject.backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalizationService {

    private final PersonalizationOptionRepository personalizationOptionRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public List<PersonalizationOptionDTO> getOptionsByProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return personalizationOptionRepository.findByProduct(product).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalizationOptionDTO createOption(Integer productId, PersonalizationOptionDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        PersonalizationOption option = new PersonalizationOption();
        option.setProduct(product);
        option.setUsiType(dto.getUsiType());
        option.setMassage(dto.getMassage());
        if (dto.getColor() != null) {
            option.setColor(PersonalizationOption.Color.valueOf(dto.getColor()));
        }
        option.setExtraPrice(dto.getExtraPrice());
        option.setMaxLength(dto.getMaxLength());
        option = personalizationOptionRepository.save(option);
        return toDto(option);
    }

    @Transactional
    public PersonalizationOptionDTO updateOption(Integer optionId, PersonalizationOptionDTO dto) {
        PersonalizationOption option = personalizationOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option not found"));
        if (dto.getUsiType() != null)
            option.setUsiType(dto.getUsiType());
        if (dto.getMassage() != null)
            option.setMassage(dto.getMassage());
        if (dto.getColor() != null)
            option.setColor(PersonalizationOption.Color.valueOf(dto.getColor()));
        if (dto.getExtraPrice() != null)
            option.setExtraPrice(dto.getExtraPrice());
        if (dto.getMaxLength() != null)
            option.setMaxLength(dto.getMaxLength());
        option = personalizationOptionRepository.save(option);
        return toDto(option);
    }

    @Transactional
    public void deleteOption(Integer optionId) {
        personalizationOptionRepository.deleteById(optionId);
    }

    private PersonalizationOptionDTO toDto(PersonalizationOption option) {
        PersonalizationOptionDTO dto = new PersonalizationOptionDTO();
        dto.setOptionId(option.getOptionId());
        dto.setProductId(option.getProduct() != null ? option.getProduct().getProductId() : null);
        dto.setUsiType(option.getUsiType());
        dto.setMassage(option.getMassage());
        dto.setColor(option.getColor() != null ? option.getColor().name() : null);
        dto.setExtraPrice(option.getExtraPrice());
        dto.setMaxLength(option.getMaxLength());
        return dto;
    }

    @Transactional
    public CartItem createOptionAndAddToCart(User user, AddPersonalizedToCartRequest request) {
        // Validate product exists
        productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Build personalization details
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        if (request.getAdditionalDetails() != null) {
            details.putAll(request.getAdditionalDetails());
        }
        if (request.getUsiType() != null)
            details.put("usiType", request.getUsiType());
        if (request.getMassage() != null)
            details.put("massage", request.getMassage());
        if (request.getColor() != null)
            details.put("color", request.getColor());
        if (request.getExtraPrice() != null)
            details.put("extraPrice", request.getExtraPrice());
        if (request.getMaxLength() != null)
            details.put("maxLength", request.getMaxLength());

        // Convert to PersonalizationDTO for consistency with new system
        PersonalizationDTO personalizationDTO = new PersonalizationDTO();
        personalizationDTO.setCustomizationId(PersonalizationDTO.generateCustomizationId());

        // Map old fields to new structure
        if (request.getUsiType() != null) {
            personalizationDTO.setOccasion(request.getUsiType());
        }
        if (request.getMassage() != null) {
            personalizationDTO.setCustomMessage(request.getMassage());
        }
        if (request.getExtraPrice() != null) {
            personalizationDTO.setExtraCost(request.getExtraPrice());
        }

        // Add any additional details to the personalization
        if (request.getAdditionalDetails() != null) {
            Map<String, Object> personalizationMap = personalizationDTO.toMap();
            personalizationMap.putAll(request.getAdditionalDetails());
            personalizationDTO = PersonalizationDTO.fromMap(personalizationMap);
        }

        // Add to cart atomically using existing cart service
        CartItem cartItem = cartService.addToCart(user, request.getProductId(), request.getQuantity(),
                personalizationDTO);
        return cartItem;
    }
}
