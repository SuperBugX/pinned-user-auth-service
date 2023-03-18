package com.superbugx.pinned.dto.responses;

import org.springframework.hateoas.EntityModel;

//The generic class is used when generating HATEOAS responses
//Best used for single data type values such as Integer, Float...etc
public class GenericResponse<T> extends EntityModel<GenericResponse<T>> {
	// Attributes
	private T data;

	// Methods
	public GenericResponse(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericResponse other = (GenericResponse) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
}
