package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.helpers.Reporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepo;
	
	
	public List<Category> listAll(String sortDir){
		
		Sort sort = Sort.by("name");
		
		 if(sortDir.equals("asc")) {
			sort = sort.ascending();
		}else if(sortDir.equals("desc")) {
			sort = sort.descending();
		}
		
		List<Category> rootCategories = categoryRepo.findRootCategories(sort);
		
		return listHierarchicalCategories(rootCategories,sortDir);
	}
	
	private List<Category> listHierarchicalCategories(List<Category> rootCategories,String sortDir){
		List<Category> hierarchicalCategories = new ArrayList<>();
		
		for(Category rootCategory : rootCategories) {
			hierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children = sortSubCategories(rootCategory.getChildren(),sortDir); 
			
			for(Category subCategory : children) {
				String name = "--" + subCategory.getName();
				hierarchicalCategories.add(Category.copyFull(subCategory,name));
				listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1, sortDir);
			}
		}
		return hierarchicalCategories;
	}
	
	private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,
			Category parent, int subLevel, String sortDir) {
		Set<Category> children = sortSubCategories(parent.getChildren(),sortDir);
		int newSubLevel = subLevel + 1;
		
		for(Category subCategory : children) {
			String name = "";
			for(int i=0; i< newSubLevel; i++) {
				name += "--";
			}
			
			name += subCategory.getName();
			hierarchicalCategories.add(Category.copyFull(subCategory, name));
			
			listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel,sortDir);
		}
		
	}
	
	public void deleteCategory(Integer id) throws CategoryNotFoundException {
		
		Long countById = categoryRepo.countById(id);
		
		if( countById == null || countById == 0) {
			throw new CategoryNotFoundException("Could not find any category with ID: " + id);
		}
		
		categoryRepo.deleteById(id);
		
	}
	
	public Category save(Category category) {
		return categoryRepo.save(category);
	}
	
	public List<Category> listCategoriesUsedInForm(){
		Iterable<Category> categoriesInDb = categoryRepo.findRootCategories(Sort.by("name").ascending());
		
		List<Category> categoriesUsedInForm = new ArrayList<>();
		
		for(Category category : categoriesInDb) {
			if(category.getParent() == null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				
				Set<Category> children =sortSubCategories( category.getChildren());
				
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
		
		Set<Category> children =sortSubCategories(parent.getChildren());
		
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
	
	private SortedSet<Category> sortSubCategories(Set<Category> children){
		return sortSubCategories(children,"asc");
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {
			@Override
			public int compare(Category cat1, Category cat2) {
				if (sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				} else {
					return cat2.getName().compareTo(cat1.getName());
				}
			}
		});
		
		sortedChildren.addAll(children);
		
		return sortedChildren;
	}
	
	public void updateEnabledStatus(Integer id, Boolean enabled) {
		categoryRepo.updateEnabledStatus(id, enabled);
	}

}
