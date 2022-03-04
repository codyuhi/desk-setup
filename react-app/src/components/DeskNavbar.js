import React from 'react'
import { Navbar, Nav, NavItem } from 'react-bootstrap'
import { LinkContainer } from 'react-router-bootstrap'

const DeskNavbar = () => {
    return (
        <Navbar>
            <Navbar.Brand>
                <LinkContainer to="/">
                    <NavItem><h1>Navbar</h1></NavItem>
                </LinkContainer>
            </Navbar.Brand>
            <Nav>
                <LinkContainer to="/">
                    <NavItem>Home</NavItem>
                </LinkContainer>
                <LinkContainer to="/studio-size">
                    <NavItem>Studio Size</NavItem>
                </LinkContainer>
                <LinkContainer to="/large-size">
                    <NavItem>Large Size</NavItem>
                </LinkContainer>
                <LinkContainer to="/contact">
                    <NavItem>Contact</NavItem>
                </LinkContainer>
            </Nav>
        </Navbar>
    )
}

export default DeskNavbar