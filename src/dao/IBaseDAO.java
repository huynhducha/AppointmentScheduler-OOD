package dao;

import java.util.List;

public interface IBaseDAO<T, K> {
    // Các thao tác CRUD (Create, Read, Update, Delete) cơ bản mà bảng nào cũng cần
    K insert(T entity);            // Trả về khóa chính (ID) sau khi thêm
    void update(K id, T entity);
    void delete(K id);
    T findById(K id);
    List<T> findAll();
}