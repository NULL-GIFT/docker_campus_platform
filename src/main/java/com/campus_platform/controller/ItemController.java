package com.campus_platform.controller;

import com.campus_platform.entity.Item;
import com.campus_platform.entity.User;
import com.campus_platform.service.ItemService;
import com.campus_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/market")
public class ItemController {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;


    /**
     * 商品列表页
     */
    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "1") int page,
                       HttpSession session,
                       Model model) {
        int pageSize = 10;
        List<Item> items = itemService.getItemList(page, pageSize);
        int totalPages = itemService.getTotalPages(pageSize);

        // 获取当前用户ID
        User currentUser = (User) session.getAttribute("user");
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isMyItems", false); // 标记为"所有商品"页面

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (Item item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", item);
            String sellerNickname = getUserNicknameById(item.getSellerId());
            map.put("sellerNickname", sellerNickname);
            itemList.add(map);
        }

        model.addAttribute("itemList", itemList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        return "market/list";
    }


    /**
     * 我发布的商品列表
     */
    @GetMapping("/my")
    public String myItems(@RequestParam(value = "page", defaultValue = "1") int page,
                          HttpSession session,
                          Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        int pageSize = 10;
        // 获取我发布的商品
        List<Item> items = itemService.getMyItems(currentUser.getId(), page, pageSize);
        int totalPages = itemService.getMyItemsTotalPages(currentUser.getId(), pageSize);

        // 构建商品列表数据
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (Item item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", item);
            String sellerNickname = getUserNicknameById(item.getSellerId());
            map.put("sellerNickname", sellerNickname);
            itemList.add(map);
        }

        // 添加模型属性
        model.addAttribute("itemList", itemList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("isMyItems", true); // 标记为"我的商品"页面

        return "market/list";
    }


    /**
     * 发布页面
     */
    @GetMapping("/publish")
    public String publishPage() {
        return "market/publish";
    }


    /**
     * 发布商品
     */
    @PostMapping("/publish")
    public String publish(@RequestParam String title,
                          @RequestParam String description,
                          @RequestParam BigDecimal price,
                          @RequestParam(required = false) MultipartFile image,
                          HttpSession session,
                          Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(price);
        item.setSellerId(currentUser.getId());
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        item.setStatus(0); // 0=在售
        item.setIsDeleted(0); // 0=未删除

        boolean success = itemService.publishItem(item, image);//图像上传
        if (success) {
            return "redirect:/market/list";
        } else {
            model.addAttribute("error", "发布失败，请检查数据格式");
            return "market/publish";
        }
    }


    /**
     * 商品详情页
     */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id,
                         HttpSession session,
                         Model model) {
        Item item = itemService.getItemById(id);

        // 商品不存在
        if (item == null) {
            model.addAttribute("error", "商品不存在或已被删除");
            return "market/detail";
        }

        String sellerNickname = getUserNicknameById(item.getSellerId());

        // 判断当前用户是否是卖家
        User currentUser = (User) session.getAttribute("user");
        boolean isSeller = false;
        if (currentUser != null && item.getSellerId() != null) {
            isSeller = currentUser.getId().equals(item.getSellerId());
        }

        model.addAttribute("item", item);
        model.addAttribute("sellerNickname", sellerNickname);
        model.addAttribute("isSeller", isSeller);

        return "market/detail";
    }


    /**
     * 用户删除自己的商品
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes attributes) { // 重定向到列表页显示

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            boolean success = itemService.deleteItem(id, user.getId());

            if (success) {
                // FlashAttribute 重定向后还能显示一次提示信息
                attributes.addFlashAttribute("msg", "商品删除成功");
            }
//
//            else {
//                attributes.addFlashAttribute("error", "删除失败：无权操作或商品已售出");
//            }

        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("error", "系统异常：" + e.getMessage());
        }

        return "redirect:/market/list";
    }



    /**
     * 搜索商品（支持全局搜索和我的商品搜索）
     */
    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword,
                         @RequestParam(value = "page", defaultValue = "1") int page,
                         @RequestParam(value = "searchMy", defaultValue = "false") boolean searchMy, // 新增参数
                         HttpSession session,
                         Model model) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return list(page, session, model);
        }

        User currentUser = (User) session.getAttribute("user");
        // 如果没有登录但尝试搜索"我的商品"，跳转到登录
        if (searchMy && currentUser == null) {
            return "redirect:/auth/login";
        }

        int pageSize = 10;
        List<Item> items;
        int totalPages;

        if (searchMy) {
            // 搜索"我的商品"
            items = itemService.searchMyItems(currentUser.getId(), keyword, page, pageSize);
            totalPages = itemService.getSearchMyItemsTotalPages(currentUser.getId(), keyword, pageSize);
        } else {
            // 搜索所有商品
            items = itemService.searchItems(keyword, page, pageSize);
            totalPages = itemService.getSearchTotalPages(keyword, pageSize);
        }

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isMyItems", searchMy); // 标记当前是"我的商品"搜索

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (Item item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("item", item);
            String sellerNickname = getUserNicknameById(item.getSellerId());
            map.put("sellerNickname", sellerNickname);
            itemList.add(map);
        }

        model.addAttribute("itemList", itemList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("searchMy", searchMy);

        return "market/list";
    }

    /**
     * 根据用户ID获取昵称
     */
    private String getUserNicknameById(Long userId) {
        try {
            User user = userService.getUserById(userId);
            //  优先使用昵称，没有就用用户名
            if (user != null) {
                if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
                    return user.getNickname();
                } else if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                    return user.getUsername();
                }
            }
            //都不可以就id
            return "用户" + userId;
        } catch (Exception e) {
            return "用户" + userId;
        }
    }
}