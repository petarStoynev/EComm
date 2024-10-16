package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.common.entity.Category;



@Controller
public class CategoryController {
	
	@Autowired
	private CategoryService categoryService;
	
	@GetMapping("/categories")
	public String listAll(@Param("sortDir") String sortDir, Model  model) {
		
		if(sortDir == null || sortDir.isEmpty()) {
			sortDir = "asc";
		}
		
		List<Category> listCategories = categoryService.listAll(sortDir);
		
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		
		model.addAttribute("listCategories",listCategories);
		model.addAttribute("reverseSortDir", reverseSortDir);
		
		return "categories/categories";
	}
	
	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		List<Category> listCategories = categoryService.listCategoriesUsedInForm();
		
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("category", new Category());
		model.addAttribute("pageTitle","Shopme - New Category");
		
		return "/categories/categories_form";
	}
	
	@PostMapping("/categories/save")
	public String saveCategory(Category category,
			@RequestParam ("fileImage") MultipartFile multipartFile,
			RedirectAttributes ra) throws IOException{
		
		if(!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);
			
			Category savedCategory = categoryService.save(category);
			String uploadDir = "../category-images/" + savedCategory.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}else {
			categoryService.save(category);
		}
		
		ra.addFlashAttribute("message", "The category has been saved successfully");
		return "redirect:/categories";
		
	}
	
	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable(name="id") Integer id, Model model, RedirectAttributes ra) {
		
		try {
			Category category = categoryService.get(id);
			List<Category> listCategories = categoryService.listCategoriesUsedInForm();
			
			model.addAttribute("category",category);
			model.addAttribute("listCategories",listCategories);
			model.addAttribute("pageTitle","Edit Category (ID:" +id + ")");
			
			return "categories/categories_form";
		} catch (CategoryNotFoundException ex) {
			ra.addFlashAttribute("message",ex.getMessage());
			
			return "redirect:/categories";
		}
			
	}
	
	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(@PathVariable("id") Integer id,
			@PathVariable("status") Boolean enabled,
			RedirectAttributes ra) {
		
		String status = enabled ? "enabled" : "disabled";
		String message = "The user with ID: " + id + " has been " + status +"!"; 
		
		categoryService.updateEnabledStatus(id, enabled);
		ra.addFlashAttribute("message",message);
		
		
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable(name = "id") Integer id,
			Model model,
			RedirectAttributes ra) {
		
		try {
			categoryService.deleteCategory(id);
			String categoryDir = "../category-images/" + id;
			FileUploadUtil.removeDir(categoryDir);
			ra.addFlashAttribute("message","Category with ID: " + id + " has been deleted successfully!");
		} catch (Exception ex) {
			ra.addFlashAttribute("message",ex.getMessage());
		}
		
		
		return "redirect:/categories";
	}
	

	

}
