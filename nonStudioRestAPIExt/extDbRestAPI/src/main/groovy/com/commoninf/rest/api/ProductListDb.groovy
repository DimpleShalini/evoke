package com.commoninf.rest.api

import java.sql.ResultSet
import java.sql.SQLException
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductListDb extends Database  {
	public class Product {
		private int id;
		private String name;
		private String project_code;
		private String inn;
		private String pharma_form;
		private String indication;
		private String therap_area;

		public Product(int id,
					   String name,
					   String project_code,
					   String inn,
					   String pharma_form,
					   String indication,
					   String therap_area) {
			this.id = id;
			this.name = name;
			this.project_code = project_code;
			this.inn = inn;
			this.pharma_form = pharma_form;
			this.indication = indication;
			this.therap_area = therap_area;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getProject_code() {
			return project_code;
		}

		public void setProject_code(String project_code) {
			this.project_code = project_code;
		}

		public String getInn() {
			return inn;
		}

		public void setInn(String inn) {
			this.inn = inn;
		}

		public String getPharma_form() {
			return pharma_form;
		}

		public void setPharma_form(String pharma_form) {
			this.pharma_form = pharma_form;
		}

		public String getIndication() {
			return indication;
		}

		public void setIndication(String indication) {
			this.indication = indication;
		}

		public String getTherap_area() {
			return therap_area;
		}

		public void setTherap_area(String therap_area) {
			this.therap_area = therap_area;
		}
		
	};
	
	private static final Logger logger = LoggerFactory.getLogger("com.commoninf.rest.api.ProductListDb");
	private ArrayList<Product> prodList;

	/**************************************************************************
	 *
	 * @param db_url
	 * @param db_port
	 * @param db_name
	 * @param db_user_name
	 * @param db_password
	 */
	public ProductListDb(String db_url, String db_port, String db_name, String db_user_name, String db_password) {
		super (db_url, db_port, db_name, db_user_name, db_password);
		
		prodList = new ArrayList<Product> ();
	}

	/**************************************************************************
	 *
	 * @return
	 */
	public ArrayList<Product> getProdList () {
		return prodList;
	}
	
	/**************************************************************************
	 *
	 * @return
	 */
	public Product getProdAt (int index) {
		Product ret_val = null;
		
		if ((prodList.size > 0)&&(index < prodList.size)) {
			ret_val = prodList.get(index);
		}
		
		return ret_val;
	}

	/**************************************************************************
	 *
	 */
	@Override
	public void getRow (ResultSet rs) throws SQLException {
		int col_cnt = 1;
		int id = rs.getInt(col_cnt++);
		String name = rs.getString (col_cnt++);
		String project_code = rs.getString (col_cnt++);
		String inn = rs.getString (col_cnt++);
		String pharma_form = rs.getString (col_cnt++);
		String indication = rs.getString (col_cnt++);
		String therap_area = rs.getString (col_cnt++);
		
		Product prod = new Product (id, name, project_code, inn, pharma_form, indication, therap_area);
		
		prodList.add (prod);
	}

	/**************************************************************************
	 *
	 */
	public void fetchAllProducts () {
		prodList = new ArrayList<Product> ();

		getDbConnection ();
		try {
			runQuery ("SELECT * FROM\r\n" + "product_lx");
		}
		catch (SQLException e) {
			prodList.add(new Product ("Error", "SQL Error while fetching all versions: " + e.getMessage ()))
			logger.info ("SQL Error while fetching all versions: " + e.getMessage ());
			e.printStackTrace ();
		}
		finally {
			closeConnection ();
		}
		logger.info ("Found "+prodList.size+" products");
	}
	
	/**************************************************************************
	 *
	 */
	public Product fetchProduct (String id) {
		prodList = new ArrayList<Product> ();

		getDbConnection ();
		try {
			runQuery ("SELECT * FROM\r\n" + "product_lx\r\n"+"WHERE id="+id+";");
		}
		catch (SQLException e) {
			prodList.add(new Product ("Error", "SQL Error while fetching all versions: " + e.getMessage ()))
			logger.info ("SQL Error while fetching all versions: " + e.getMessage ());
			e.printStackTrace ();
		}
		finally {
			closeConnection ();
		}
		
		return getProdAt(0);
	}
}
