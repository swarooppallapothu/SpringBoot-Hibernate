package app.controllers;

import app.models.User;
import app.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserRestController {

    @Autowired
    private UserDao _userDao;

    @RequestMapping(method = RequestMethod.DELETE)
    public String delete(int id) {
        try {
            User user = new User(id);
            _userDao.delete(user);
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return "User succesfully deleted!";
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> getAll() {
        return new ResponseEntity<>(_userDao.getAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public ResponseEntity<?> getByUserId(@PathVariable Integer userId) {
        return new ResponseEntity<>(_userDao.get(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/get-by-email", method = RequestMethod.GET)
    public ResponseEntity<?> getByEmail(@RequestParam(name = "email") String email) {
        User user = _userDao.getByEmail(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<?> create(@ModelAttribute User user) {
        _userDao.save(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
