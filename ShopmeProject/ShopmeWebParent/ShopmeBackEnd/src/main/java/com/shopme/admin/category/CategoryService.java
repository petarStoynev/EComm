package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.helpers.Reporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	
	public List<Category> listAll(){
		List<Category> rootCategories = categoryRepo.findRootCategories();
		
		return listHierarchicalCategories(rootCategories);
	}
	
	private List<Category> listHierarchicalCategories(List<Category> rootCategories){
		List<Category> hierarchicalCategories = new ArrayList<>();
		
		for(Category rootCategory : rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children = rootCategory.getChildren();
			
			for(Category subCategory : children) {
				String name = "--" + subCategory.getName();
				hierarchicalCategories.add(Category.copyFull(subCategory,name));
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1);
			}
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,
			Category parent, int subLevel) {
		Set<Category> children = parent.getChildren();
		int newSubLevel = subLevel + 1;
		
		for(Category subCategory : children) {
			String name = "";
			for(int i=0; i< newSubLevel; i++) {
				name += "--";
			}
			
			name += subCategory.getName();
			hierarchicalCategories.add(Category.copyFull(subCategory, name));
			
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel);
		}
		
	}
	
	public Category save(Category category) {
		return categoryRepo.save(category);
	}
	
	public List<Category> listCategoriesUsedInForm(){
		Iterable<Category> categoriesInDb = categoryRepo.findAll();
		
		List<Category> categoriesUsedInForm = new ArrayList<>();
		
		for(Category category : categoriesInDb) {
			if(category.getParent() == null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				
				Set<Category> children = category.getChildren();
				
				for(Category subCategory : children) {
					String name = "--" + subCategory.getName();
					categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
					
					listSubCategoriesUsedInForm(categoriesUsedInForm, category, 0);
				}
			}
		}
		
		return categoriesUsedInForm;
		
	}

	private void listSubCategoriesUsedInForm(List<Category> categoriesUsedInForm ,Category parent, int subLevel) {
		
		int newSubLevel = subLevel + 1;
		
		Set<Category> children = parent.getChildren();
		
		for(Category subCategory : children) {
			String name = "";
			for(int i=0; i< newSubLevel; i++) {
				name += "--";
			}
			
			name += subCategory.getName();
			categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			
			listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, newSubLevel);
		}
		
	}

	public Category get(Integer id) throws CategoryNotFoundException{
		try {
			return categoryRepo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new CategoryNotFoundException("Could not find any cateogory with ID: " + id);
		}
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		
		Category categoryByName = categoryRepo.findByName(name);
		
		if(isCreatingNew) {
			if(categoryByName != null) {
				return "Duplicate name";
			}else {
				Category categoryByAlias = categoryRepo.findByAlias(alias);
				if(categoryByAlias != null) {
					return "Duplicate alias";
				}
			}
		}else {
			if(categoryByName != null && categoryByName.getId() != id) {
				return "Duplicate name";
			}
			Category categoryByAlias = categoryRepo.findByAlias(alias);
			if(categoryByAlias != null && categoryByAlias.getId() != id) {
				return "Duplicate alias";
			}
		}
		
		return "OK";
	}

}
