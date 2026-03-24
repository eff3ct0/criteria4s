import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  docsSidebar: [
    'intro',
    'getting-started',
    {
      type: 'category',
      label: 'Concepts',
      items: ['concepts/type-classes', 'concepts/tagless-final', 'concepts/architecture', 'concepts/extensibility'],
    },
    {
      type: 'category',
      label: 'User Guide',
      items: ['guides/predicates', 'guides/conjunctions', 'guides/transforms', 'guides/clauses', 'guides/extensions-vs-functions'],
    },
    {
      type: 'category',
      label: 'Dialects',
      items: ['dialects/sql', 'dialects/postgresql', 'dialects/mysql', 'dialects/sparksql', 'dialects/mongodb', 'dialects/elasticsearch'],
    },
    {
      type: 'category',
      label: 'Client Integrations',
      items: ['integrations/jdbc', 'integrations/mongodb-driver', 'integrations/elasticsearch-client'],
    },
    {
      type: 'category',
      label: 'Contributing',
      items: ['contributing/development', 'contributing/new-dialect', 'contributing/architecture-decisions'],
    },
  ],
};

export default sidebars;
