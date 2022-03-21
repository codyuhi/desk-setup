import { BrowserRouter, Route, Routes } from 'react-router-dom';
import './App.css';

// custom components
import DeskNavbar from './components/desk-navbar/DeskNavbar'
import Home from './components/home/Home'
import StudioSize from './components/studio-size/StudioSize';
import LargeSize from './components/large-size/LargeSize'
import PageNotFound from './components/PageNotFound';
import Footer from './components/Footer'
import Contact from './components/contact/Contact';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <DeskNavbar />
        <Routes>
          <Route path="/" exact element={<Home />} />
          <Route path="/studio-size" exact element={<StudioSize />} />
          <Route path="/large-size" exact element={<LargeSize />} />
          <Route path="/contact" element={<Contact />} />
          <Route path="*" element={<PageNotFound />} />
        </Routes>
        <Footer />
      </BrowserRouter>
    </div>
  );
}

export default App;
