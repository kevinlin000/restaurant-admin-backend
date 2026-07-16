package com.restaurant.order.service;

import com.restaurant.common.BusinessException;
import com.restaurant.member.entity.User;
import com.restaurant.member.repository.UserRepository;
import com.restaurant.member.service.PointService;
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.entity.StoreMenu;
import com.restaurant.menu.repository.MenuItemRepository;
import com.restaurant.menu.repository.StoreMenuRepository;
import com.restaurant.order.dto.CreateOrderRequest;
import com.restaurant.order.dto.OrderItemRequest;
import com.restaurant.order.dto.OrderResponse;
import com.restaurant.order.entity.Order;
import com.restaurant.order.entity.Payment;
import com.restaurant.order.repository.OrderItemRepository;
import com.restaurant.order.repository.OrderRepository;
import com.restaurant.order.repository.PaymentRepository;
import com.restaurant.store.entity.Store;
import com.restaurant.store.entity.TableInfo;
import com.restaurant.store.repository.StoreRepository;
import com.restaurant.store.repository.TableInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private TableInfoRepository tableInfoRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private StoreMenuRepository storeMenuRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderUsesStoreMenuPriceAndStoreScopedTable() {
        CreateOrderRequest request = orderRequest("DINE_IN", 9L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(openStore()));
        when(tableInfoRepository.findByTableIdAndStoreId(9L, 2L)).thenReturn(Optional.of(table()));
        when(storeMenuRepository.findByStoreIdAndMenuItemIdAndIsAvailableTrue(2L, 11L))
                .thenReturn(Optional.of(storeMenu(new BigDecimal("90.00"))));
        when(menuItemRepository.findById(11L)).thenReturn(Optional.of(menuItem(new BigDecimal("100.00"))));
        when(pointService.usePointsForOrder(1L, 2L, null, 10)).thenReturn(10);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(100L);
            return order;
        });
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Payment.builder().paymentMethod("CASH").build());
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(List.of());

        OrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(response.getStoreId()).isEqualTo(2L);
        assertThat(response.getTableId()).isEqualTo(9L);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("180.00");
        assertThat(savedOrder.getFinalAmount()).isEqualByComparingTo("170.00");
        assertThat(savedOrder.getPointsUsed()).isEqualTo(10);
        assertThat(savedOrder.getTable().getTableId()).isEqualTo(9L);
        verify(tableInfoRepository).findByTableIdAndStoreId(9L, 2L);
        verify(pointService).usePointsForOrder(1L, 2L, null, 10);
    }

    @Test
    void createOrderRejectsTableFromAnotherStore() {
        CreateOrderRequest request = orderRequest("DINE_IN", 9L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(openStore()));
        when(tableInfoRepository.findByTableIdAndStoreId(9L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("桌位不屬於指定門市");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderRejectsMenuItemUnavailableInStore() {
        CreateOrderRequest request = orderRequest("DINE_IN", 9L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(openStore()));
        when(tableInfoRepository.findByTableIdAndStoreId(9L, 2L)).thenReturn(Optional.of(table()));
        when(storeMenuRepository.findByStoreIdAndMenuItemIdAndIsAvailableTrue(2L, 11L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("此門市未供應部分餐點");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createTakeoutOrderAllowsNoTableAndNormalizesOrderType() {
        CreateOrderRequest request = orderRequest("TAKE_OUT", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(storeRepository.findByStoreIdAndIsDeletedFalse(2L)).thenReturn(Optional.of(openStore()));
        when(storeMenuRepository.findByStoreIdAndMenuItemIdAndIsAvailableTrue(2L, 11L))
                .thenReturn(Optional.of(storeMenu(null)));
        when(menuItemRepository.findById(11L)).thenReturn(Optional.of(menuItem(new BigDecimal("100.00"))));
        when(pointService.usePointsForOrder(1L, 2L, null, 10)).thenReturn(10);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setOrderId(101L);
            return order;
        });
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Payment.builder().paymentMethod("CASH").build());
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(List.of());

        OrderResponse response = orderService.createOrder(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        assertThat(response.getTableId()).isNull();
        assertThat(orderCaptor.getValue().getOrderType()).isEqualTo("TAKEOUT");
        assertThat(orderCaptor.getValue().getTable()).isNull();
        assertThat(orderCaptor.getValue().getPointsUsed()).isEqualTo(10);
        verifyNoInteractions(tableInfoRepository);
        verify(pointService).usePointsForOrder(1L, 2L, null, 10);
    }

    private static CreateOrderRequest orderRequest(String orderType, Long tableId) {
        OrderItemRequest item = new OrderItemRequest();
        item.setMenuItemId(11L);
        item.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setStoreId(2L);
        request.setTableId(tableId);
        request.setOrderType(orderType);
        request.setPointsUsed(10);
        request.setPaymentMethod("CASH");
        request.setItems(List.of(item));
        return request;
    }

    private static User user() {
        return User.builder().userId(1L).build();
    }

    private static Store openStore() {
        return Store.builder()
                .storeId(2L)
                .status("OPEN")
                .isDeleted(false)
                .build();
    }

    private static TableInfo table() {
        return TableInfo.builder()
                .tableId(9L)
                .storeId(2L)
                .build();
    }

    private static MenuItem menuItem(BigDecimal price) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(11L);
        menuItem.setItemName("敘日套餐");
        menuItem.setPrice(price);
        menuItem.setIsActive(true);
        return menuItem;
    }

    private static StoreMenu storeMenu(BigDecimal price) {
        StoreMenu storeMenu = new StoreMenu();
        storeMenu.setStoreId(2L);
        storeMenu.setMenuItemId(11L);
        storeMenu.setPrice(price);
        storeMenu.setIsAvailable(true);
        return storeMenu;
    }
}
