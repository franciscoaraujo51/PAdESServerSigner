package com.padesserversigner.PadesServerSigner.Repository;

import com.padesserversigner.PadesServerSigner.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM user WHERE token = :token", nativeQuery = true)
    List<User> findAuthorsByFirstName(@Param("token") String token);

    @Query(value = "SELECT * FROM user WHERE phone_number = :phoneNumber LIMIT 1;", nativeQuery = true)
    User getByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query(value = "SELECT * FROM user WHERE credential_id = :credential_id LIMIT 1;", nativeQuery = true)
    User getByPCredentialID(@Param("credential_id") String credential_id);

}