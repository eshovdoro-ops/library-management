package com.library.repository;

import com.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    /** pattern — уже в нижнем регистре и с символами %, параметр всегда не null. */
    @Query("""
           select b from Book b
           where (lower(b.title) like :pattern
               or lower(b.author) like :pattern
               or lower(coalesce(b.isbn, '')) like :pattern)
             and (:onlyAvailable = false or b.availableCopies > 0)
           """)
    Page<Book> search(@Param("pattern") String pattern,
                      @Param("onlyAvailable") boolean onlyAvailable,
                      Pageable pageable);
}
