package com.shopme.admin.category;



import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.shopme.common.entity.Category;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>, CrudRepository<Category, Integer> {

	@Query("SELECT c FROM Category c WHERE parent.id is NULL")
	public List<Category> findRootCategories();
	
	public Category findByName(String name);
	
	public Category findByAlias(String alias);

	

	

}
