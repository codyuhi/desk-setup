import './App.css';

// custom components
import DeskNavbar from './components/DeskNavbar'
console.log(typeof(<DeskNavbar/>))

function App() {
  return (
    <div className="App">
      <DeskNavbar />
    </div>
  );
}

export default App;
