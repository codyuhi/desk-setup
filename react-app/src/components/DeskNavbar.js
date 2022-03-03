import React from 'react'
import { Navbar, Nav, NavItem } from 'react-bootstrap'

const DeskNavbar = () => {
    return (
        <Navbar>
            <Navbar.Header>
                <Navbar.Brand>
                    <a href="/"></a>
                </Navbar.Brand>
            </Navbar.Header>
            <Nav>
                <NavItem eventKey={1} href="/">Home</NavItem>
                <NavItem eventKey={2} href="/studio-size">Studio Size</NavItem>
                <NavItem eventKey={3} href="/large-size">Large Size</NavItem>
                <NavItem eventKey={4} href="/contact">Contact</NavItem>
            </Nav>
        </Navbar>
    )
}

export default DeskNavbar