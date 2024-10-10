package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.List;
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
		return (List<Category>) categoryRepo.findAll();
	}
	
	public List<Category> listCategoriesUsedInForm(){
		Iterable<Category> categoriesInDb = categoryRepo.findAll();
		
		List<Category> categoriesUsedInForm = new ArrayList<>();
		
		for(Category category : categoriesInDb) {
			if(category.getParent() == null) {
				categoriesUsedInForm.add(new Category(category.getName()));
				
				Set<Category> children = category.getChildren();
				
				for(Category subCategory : children) {
					String name = "--" + subCategory.getName();
					categoriesUsedInForm.add(new Category(name));
					
					listChildren(categoriesUsedInForm,subCategory, 1);
				}
			}
		}
		
		return categoriesUsedInForm;
		
	}

	private void listChildren(List<Category> categoriesUsedInForm ,Category parent, int subLevel) {
		
		int newSubLevel = subLevel + 1;
		
		Set<Category> children = parent.getChildren();
		
		for(Category subCategory : children) {
			String name = "";
			for(int i=0; i< newSubLevel; i++) {
				name += "--";
			}
			
			name += subCategory.getName();
			categoriesUsedInForm.add(new Category(name));
			
			listChildren(categoriesUsedInForm, subCategory, newSubLevel);
		}
		
	}

}
