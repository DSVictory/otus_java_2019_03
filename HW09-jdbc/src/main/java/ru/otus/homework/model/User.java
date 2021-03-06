package ru.otus.homework.model;

import ru.otus.homework.jdbc.Id;

public class User {

	@Id
	private long id;
	
	private String name;
	
	private int age;
	
	public User() {}
	
	public User(long id, String name, int age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getAge() {
		return age;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setAge(int age) {
		this.age = age;
	}
	
	@Override
	public String toString() {
		return "User: id = " + id
				+ ", name = " + name
				+ ", age = " + age;
	}
	
}
